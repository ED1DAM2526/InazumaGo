#!/usr/bin/env python3
"""Genera un .md con un resumen rico de participación en Taiga por alumno.

Además de contar actuaciones por entidad (user stories, tasks, issues y epics),
clasifica el historial para detectar tipos de participación más útiles:

- comentarios
- cambios de estado
- asignaciones
- cierres y reaperturas
- cambios de prioridad / severidad / tipo
- cambios de planificación
- cambios de contenido (título / descripción)
- otros cambios

Una misma entrada de historial puede aportar a varias categorías.
"""

from __future__ import annotations

import json
from collections import Counter, defaultdict
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Dict, List, Optional, Set
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode
from urllib.request import Request, urlopen


BASE_URL = "https://api.taiga.io/api/v1"
PROJECT_SLUG = "sergiolozanoprofe-prueba"
YEAR = datetime.now(timezone.utc).year
DATE_FROM = f"{YEAR}-05-01"
DATE_TO = f"{YEAR}-05-26"
PAGE_SIZE = 100

ASSIGNMENT_KEYS = {
    "assigned_to",
    "assigned_users",
    "owner",
    "owners",
    "watchers",
}
CLASSIFICATION_KEYS = {"priority", "severity", "type"}
PLANNING_KEYS = {
    "milestone",
    "due_date",
    "finish_date",
    "start_date",
    "estimated_start",
    "estimated_finish",
    "estimated_hours",
    "kanban_order",
    "sprint_order",
    "backlog_order",
}
CONTENT_KEYS = {"subject", "description", "content", "name"}
ATTACHMENT_KEYS = {"attachments", "attachment", "attached_file"}


def read_token(token_file: Path) -> str:
    for line in token_file.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if line.lower().startswith("auth_token:"):
            return line.split(":", 1)[1].strip()
    raise RuntimeError("No auth_token found")


def api_get(path: str, token: str, params: Optional[Dict[str, Any]] = None) -> Any:
    query = ""
    if params:
        query = "?" + urlencode(params)
    url = f"{BASE_URL}{path}{query}"
    headers = {
        "Authorization": f"Bearer {token}",
        "Accept": "application/json",
        "User-Agent": "taiga-md-summary/2.0",
    }
    req = Request(url=url, headers=headers, method="GET")
    try:
        with urlopen(req, timeout=30) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except HTTPError as exc:
        body = exc.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"HTTP {exc.code} {url}: {body[:200]}")
    except URLError as exc:
        raise RuntimeError(f"Network error {url}: {exc}")


def fetch_paginated(path: str, token: str, params: Dict[str, Any]) -> List[Dict[str, Any]]:
    page = 1
    out: List[Dict[str, Any]] = []
    repeated_first_id = None

    while True:
        p = dict(params)
        p["page"] = page
        p["page_size"] = PAGE_SIZE
        data = api_get(path, token, params=p)
        if not isinstance(data, list):
            break
        if not data:
            break

        first_id = data[0].get("id")
        if repeated_first_id is not None and first_id == repeated_first_id:
            break
        repeated_first_id = first_id

        out.extend(data)
        if len(data) < PAGE_SIZE:
            break
        page += 1
    return out


def iso_to_dt(value: str) -> datetime:
    if value.endswith("Z"):
        value = value[:-1] + "+00:00"
    return datetime.fromisoformat(value)


def safe_changed(values: Any) -> bool:
    if isinstance(values, list) and len(values) >= 2:
        return values[0] != values[1]
    return True


def classify_history_row(row: Dict[str, Any]) -> Counter:
    diff = row.get("diff") or {}
    comment = (row.get("comment") or "").strip()
    categories = Counter()

    if comment:
        categories["comments"] += 1

    if "status" in diff and safe_changed(diff.get("status")):
        categories["status_changes"] += 1

    assignment_hits = [key for key in ASSIGNMENT_KEYS if key in diff and safe_changed(diff.get(key))]
    if assignment_hits:
        categories["assignments"] += 1

    if "is_closed" in diff and isinstance(diff.get("is_closed"), list) and len(diff["is_closed"]) >= 2:
        before, after = diff["is_closed"][0], diff["is_closed"][1]
        if before is False and after is True:
            categories["closures"] += 1
        elif before is True and after is False:
            categories["reopens"] += 1
        else:
            categories["closure_related"] += 1

    if any(key in diff and safe_changed(diff.get(key)) for key in CLASSIFICATION_KEYS):
        categories["classification_changes"] += 1

    if any(key in diff and safe_changed(diff.get(key)) for key in PLANNING_KEYS):
        categories["planning_changes"] += 1

    if any(key in diff and safe_changed(diff.get(key)) for key in CONTENT_KEYS):
        categories["content_changes"] += 1

    if any(key in diff and safe_changed(diff.get(key)) for key in ATTACHMENT_KEYS):
        categories["attachment_changes"] += 1

    categorized_keys: Set[str] = (
        {"status", "is_closed"}
        | ASSIGNMENT_KEYS
        | CLASSIFICATION_KEYS
        | PLANNING_KEYS
        | CONTENT_KEYS
        | ATTACHMENT_KEYS
    )
    other_keys = [key for key in diff if key not in categorized_keys and safe_changed(diff.get(key))]
    if other_keys:
        categories["other_changes"] += 1

    if diff:
        categories["events_with_diff"] += 1

    return categories


def compute_teacher_score(row: Dict[str, Any]) -> int:
    """Puntuación simple orientada a evaluación docente.

    Da más peso a actividad sostenida y acciones significativas.
    """
    score = 0
    score += row["days"] * 3
    score += row["events"]
    score += row["comments"] * 2
    score += row["status_changes"] * 2
    score += row["assignments"]
    score += row["closures"] * 3
    score += row["reopens"] * 2
    score += row["classification_changes"]
    score += row["planning_changes"]
    score += row["content_changes"] * 2
    score += row["attachment_changes"]
    return score


def teacher_level(row: Dict[str, Any]) -> str:
    if row["events"] == 0:
        return "Nula"
    if row["score"] >= 90 or (row["days"] >= 6 and row["events"] >= 25):
        return "Alta"
    if row["score"] >= 35 or (row["days"] >= 3 and row["events"] >= 8):
        return "Media"
    return "Baja"


def main() -> int:
    script_dir = Path(__file__).resolve().parent
    token_file = script_dir / "taiga.md"
    token = read_token(token_file)

    dt_from = datetime.fromisoformat(DATE_FROM).replace(tzinfo=timezone.utc)
    dt_to = datetime.fromisoformat(DATE_TO).replace(
        tzinfo=timezone.utc, hour=23, minute=59, second=59, microsecond=999999
    )

    project = api_get("/projects/by_slug", token, params={"slug": PROJECT_SLUG})
    project_id = int(project["id"])

    memberships = api_get("/memberships", token, params={"project": project_id})
    members_info: Dict[int, Dict[str, str]] = {}
    for m in memberships:
        uid = m.get("user")
        if not isinstance(uid, int):
            continue
        members_info[uid] = {
            "name": m.get("full_name") or str(uid),
            "email": m.get("user_email") or "",
        }

    sources = [
        ("/userstories", "userstory"),
        ("/tasks", "task"),
        ("/issues", "issue"),
        ("/epics", "epic"),
    ]

    by_entity: Dict[int, Counter] = defaultdict(Counter)
    by_category: Dict[int, Counter] = defaultdict(Counter)
    active_days: Dict[int, Set[Any]] = defaultdict(set)
    seen_history_ids: Set[str] = set()

    for source_path, hist_name in sources:
        entity_name = source_path.strip("/")
        print(f"Procesando {source_path}...")
        entities = fetch_paginated(source_path, token, params={"project": project_id})
        for ent in entities:
            ent_id = ent.get("id")
            if not ent_id:
                continue
            try:
                rows = api_get(f"/history/{hist_name}/{ent_id}", token)
            except Exception:
                continue
            if not isinstance(rows, list):
                continue

            for row in rows:
                history_id = row.get("id")
                if history_id in seen_history_ids:
                    continue
                seen_history_ids.add(history_id)

                created_at = row.get("created_at")
                if not created_at:
                    continue
                when = iso_to_dt(created_at)
                if when < dt_from or when > dt_to:
                    continue

                user = row.get("user") or {}
                uid = user.get("pk") if isinstance(user, dict) else None
                if not isinstance(uid, int):
                    continue

                if uid not in members_info:
                    members_info[uid] = {
                        "name": user.get("name") or str(uid),
                        "email": "",
                    }

                by_entity[uid][entity_name] += 1
                by_category[uid]["total_events"] += 1
                active_days[uid].add(when.date())

                categories = classify_history_row(row)
                for key, value in categories.items():
                    by_category[uid][key] += value

    md_lines: List[str] = []
    md_lines.append(f"# Resumen de actividad Taiga — {PROJECT_SLUG}\n")
    md_lines.append(f"Periodo: {DATE_FROM} — {DATE_TO}\n")
    md_lines.append(
        "> Nota: una misma entrada de historial puede contar en varias categorías "
        "(por ejemplo, comentario + cambio de estado + asignación)."
    )
    md_lines.append("")
    rows = []
    for uid, info in members_info.items():
        entity_counts = by_entity[uid]
        category_counts = by_category[uid]
        row = {
            "uid": uid,
            "name": info.get("name", str(uid)),
            "email": info.get("email", ""),
            "days": len(active_days[uid]),
            "events": category_counts.get("total_events", 0),
            "userstories": entity_counts.get("userstories", 0),
            "tasks": entity_counts.get("tasks", 0),
            "issues": entity_counts.get("issues", 0),
            "epics": entity_counts.get("epics", 0),
            "comments": category_counts.get("comments", 0),
            "status_changes": category_counts.get("status_changes", 0),
            "assignments": category_counts.get("assignments", 0),
            "closures": category_counts.get("closures", 0),
            "reopens": category_counts.get("reopens", 0),
            "classification_changes": category_counts.get("classification_changes", 0),
            "planning_changes": category_counts.get("planning_changes", 0),
            "content_changes": category_counts.get("content_changes", 0),
            "attachment_changes": category_counts.get("attachment_changes", 0),
            "other_changes": category_counts.get("other_changes", 0),
        }
        row["score"] = compute_teacher_score(row)
        row["teacher_level"] = teacher_level(row)
        rows.append(row)

    rows.sort(key=lambda r: (-r["score"], -r["events"], -r["days"], r["name"].lower()))

    md_lines.append("## Ranking docente\n")
    md_lines.append(
        "| Puesto | Alumno | Nivel | Puntuación | Eventos | Días activos | Coment. | Estados | Asign. | Cierres | Contenido |"
    )
    md_lines.append("|---:|---|---|---:|---:|---:|---:|---:|---:|---:|---:|")
    for pos, row in enumerate(rows, start=1):
        md_lines.append(
            f"| {pos} | {row['name']} | **{row['teacher_level']}** | {row['score']} | {row['events']} | {row['days']} | "
            f"{row['comments']} | {row['status_changes']} | {row['assignments']} | {row['closures']} | {row['content_changes']} |"
        )

    md_lines.append("")
    md_lines.append("### Criterio docente orientativo\n")
    md_lines.append("- **Alta**: participación frecuente y sostenida, con volumen relevante de eventos y/o varios días activos.")
    md_lines.append("- **Media**: participación apreciable, pero menos continua o con menor variedad de acciones.")
    md_lines.append("- **Baja**: actividad puntual o escasa.")
    md_lines.append("- **Nula**: sin actividad detectada en el periodo.")
    md_lines.append("")

    md_lines.append("## Resumen global por alumno\n")
    md_lines.append(
        "| Alumno | Email | Días activos | Eventos | US | Tasks | Issues | Epics | Coment. | Estados | Asign. | Cierres | Reap. | Clasif. | Planif. | Contenido | Adj. | Otros |"
    )
    md_lines.append(
        "|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|"
    )

    for row in rows:
        md_lines.append(
            f"| {row['name']} | {row['email']} | {row['days']} | {row['events']} | "
            f"{row['userstories']} | {row['tasks']} | {row['issues']} | {row['epics']} | "
            f"{row['comments']} | {row['status_changes']} | {row['assignments']} | "
            f"{row['closures']} | {row['reopens']} | {row['classification_changes']} | "
            f"{row['planning_changes']} | {row['content_changes']} | {row['attachment_changes']} | {row['other_changes']} |"
        )

    md_lines.append("")
    md_lines.append("## Interpretación rápida\n")
    md_lines.append("- **Eventos**: nº de entradas de historial atribuidas al alumno en el periodo.")
    md_lines.append("- **Días activos**: nº de días distintos con al menos una actuación.")
    md_lines.append("- **Estados**: cambios de estado detectados (`status`).")
    md_lines.append("- **Asign.**: cambios de asignación o responsables detectados.")
    md_lines.append("- **Cierres / Reap.**: cambios en `is_closed`.")
    md_lines.append("- **Clasif.**: cambios de prioridad, severidad o tipo.")
    md_lines.append("- **Planif.**: cambios de fechas, sprint/milestone u ordenaciones de trabajo.")
    md_lines.append("- **Contenido**: cambios en título/nombre/descripcion.")
    md_lines.append("- **Adj.**: cambios relacionados con adjuntos.")
    md_lines.append("- **Otros**: cambios no clasificados en las categorías anteriores.")
    md_lines.append("")
    md_lines.append("## Detalle por alumno\n")

    for row in rows:
        md_lines.append(f"### {row['name']}")
        md_lines.append(f"- Email: `{row['email']}`" if row["email"] else "- Email: _(sin email)_")
        md_lines.append(f"- Eventos totales: **{row['events']}**")
        md_lines.append(f"- Días activos: **{row['days']}**")
        md_lines.append(
            f"- Por entidad: userstories={row['userstories']}, tasks={row['tasks']}, issues={row['issues']}, epics={row['epics']}"
        )
        md_lines.append(
            f"- Participación detectada: comentarios={row['comments']}, estados={row['status_changes']}, "
            f"asignaciones={row['assignments']}, cierres={row['closures']}, reaperturas={row['reopens']}, "
            f"clasificación={row['classification_changes']}, planificación={row['planning_changes']}, "
            f"contenido={row['content_changes']}, adjuntos={row['attachment_changes']}, otros={row['other_changes']}"
        )
        md_lines.append("")

    out_md = script_dir / "taiga_activity_summary.md"
    out_md.write_text("\n".join(md_lines), encoding="utf-8")
    print(f"Resumen generado: {out_md}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())





