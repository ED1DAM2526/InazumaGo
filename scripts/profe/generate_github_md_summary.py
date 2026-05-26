#!/usr/bin/env python3
"""Genera resumen de actividad GitHub por alumno con ranking docente.

Lee token desde scripts/profe/github.md y consulta actividad de un repo entre
fechas hardcodeadas en este archivo.
"""

from __future__ import annotations

import json
from collections import Counter, defaultdict
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Dict, Iterable, List, Optional, Set, Tuple
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode
from urllib.request import Request, urlopen


API_BASE = "https://api.github.com"
REPO_OWNER = "ED1DAM2526"
REPO_NAME = "InazumaGo"
YEAR = datetime.now(timezone.utc).year
DATE_FROM = f"{YEAR}-05-01"
DATE_TO = f"{YEAR}-05-26"
PER_PAGE = 100


def to_iso_start(day: str) -> str:
    return f"{day}T00:00:00Z"


def to_iso_end(day: str) -> str:
    return f"{day}T23:59:59Z"


def parse_iso(value: str) -> datetime:
    if value.endswith("Z"):
        value = value[:-1] + "+00:00"
    return datetime.fromisoformat(value)


def in_range(iso_value: Optional[str], dt_from: datetime, dt_to: datetime) -> bool:
    if not iso_value:
        return False
    ts = parse_iso(iso_value)
    return dt_from <= ts <= dt_to


def read_token(token_file: Path) -> str:
    if not token_file.exists():
        raise RuntimeError(f"No existe {token_file}")

    for raw in token_file.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        lower = line.lower()
        if lower.startswith("auth_token:") or lower.startswith("token:"):
            token = line.split(":", 1)[1].strip()
            if token:
                return token
    raise RuntimeError("No se encontro token en scripts/profe/github.md (usa token: ...)")


def api_get(path: str, token: str, params: Optional[Dict[str, Any]] = None) -> Any:
    query = ""
    if params:
        query = "?" + urlencode(params)
    url = f"{API_BASE}{path}{query}"
    req = Request(
        url,
        headers={
            "Authorization": f"Bearer {token}",
            "Accept": "application/vnd.github+json",
            "X-GitHub-Api-Version": "2022-11-28",
            "User-Agent": "github-activity-summary/1.0",
        },
        method="GET",
    )
    try:
        with urlopen(req, timeout=45) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except HTTPError as exc:
        body = exc.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"HTTP {exc.code} en {url}: {body[:250]}")
    except URLError as exc:
        raise RuntimeError(f"Error de red en {url}: {exc}")


def paged(path: str, token: str, params: Dict[str, Any]) -> Iterable[Dict[str, Any]]:
    page = 1
    while True:
        req = dict(params)
        req["per_page"] = PER_PAGE
        req["page"] = page
        data = api_get(path, token, req)
        if not isinstance(data, list) or not data:
            break
        for item in data:
            yield item
        if len(data) < PER_PAGE:
            break
        page += 1


def actor_login(obj: Dict[str, Any], key: str = "user") -> Optional[str]:
    user = obj.get(key)
    if isinstance(user, dict):
        return user.get("login")
    return None


def compute_teacher_score(row: Dict[str, Any]) -> int:
    score = 0
    score += row["days_active"] * 3
    score += row["events"]
    score += row["commits"] * 2
    score += row["prs_opened"] * 3
    score += row["prs_merged"] * 4
    score += row["issues_opened"] * 2
    score += row["issues_closed"] * 2
    score += row["reviews"] * 2
    score += row["comments"]
    score += row["assignments"]
    score += row["closures"] * 2
    score += row["reopens"]
    return score


def teacher_level(row: Dict[str, Any]) -> str:
    if row["events"] == 0:
        return "Nula"
    if row["score"] >= 70 or (row["days_active"] >= 6 and row["events"] >= 20):
        return "Alta"
    if row["score"] >= 25 or (row["days_active"] >= 3 and row["events"] >= 8):
        return "Media"
    return "Baja"


def ensure_user(metrics: Dict[str, Counter], days: Dict[str, Set[Any]], login: str) -> None:
    _ = metrics[login]
    _ = days[login]


def main() -> int:
    if REPO_OWNER == "OWNER" or REPO_NAME == "REPO":
        raise RuntimeError("Configura REPO_OWNER y REPO_NAME en el script")

    script_dir = Path(__file__).resolve().parent
    token = read_token(script_dir / "github.md")

    dt_from = parse_iso(to_iso_start(DATE_FROM))
    dt_to = parse_iso(to_iso_end(DATE_TO))
    since = to_iso_start(DATE_FROM)

    repo = f"/{REPO_OWNER}/{REPO_NAME}"
    metrics: Dict[str, Counter] = defaultdict(Counter)
    days: Dict[str, Set[Any]] = defaultdict(set)
    names: Dict[str, str] = {}

    print("Recolectando commits...")
    for commit in paged(f"/repos{repo}/commits", token, {"since": since, "until": to_iso_end(DATE_TO)}):
        login = actor_login(commit, "author")
        if not login:
            continue
        ensure_user(metrics, days, login)
        dt = parse_iso(commit["commit"]["author"]["date"])
        metrics[login]["events"] += 1
        metrics[login]["commits"] += 1
        days[login].add(dt.date())
        names[login] = login

    print("Recolectando pull requests...")
    for pr in paged(f"/repos{repo}/pulls", token, {"state": "all", "sort": "updated", "direction": "desc"}):
        if not in_range(pr.get("created_at"), dt_from, dt_to) and not in_range(pr.get("closed_at"), dt_from, dt_to):
            if pr.get("updated_at") and parse_iso(pr["updated_at"]) < dt_from:
                break
            continue

        login = actor_login(pr)
        if login:
            ensure_user(metrics, days, login)
            names[login] = login
            if in_range(pr.get("created_at"), dt_from, dt_to):
                metrics[login]["events"] += 1
                metrics[login]["prs_opened"] += 1
                days[login].add(parse_iso(pr["created_at"]).date())
            if pr.get("merged_at") and in_range(pr.get("merged_at"), dt_from, dt_to):
                metrics[login]["events"] += 1
                metrics[login]["prs_merged"] += 1
                metrics[login]["closures"] += 1
                days[login].add(parse_iso(pr["merged_at"]).date())

        # Reviews del PR
        number = pr.get("number")
        if not number:
            continue
        try:
            reviews = api_get(f"/repos{repo}/pulls/{number}/reviews", token)
        except Exception:
            reviews = []
        if isinstance(reviews, list):
            for review in reviews:
                if not in_range(review.get("submitted_at"), dt_from, dt_to):
                    continue
                reviewer = actor_login(review)
                if not reviewer:
                    continue
                ensure_user(metrics, days, reviewer)
                names[reviewer] = reviewer
                metrics[reviewer]["events"] += 1
                metrics[reviewer]["reviews"] += 1
                days[reviewer].add(parse_iso(review["submitted_at"]).date())

    print("Recolectando issues...")
    for issue in paged(f"/repos{repo}/issues", token, {"state": "all", "since": since, "sort": "updated", "direction": "desc"}):
        if "pull_request" in issue:
            continue
        login = actor_login(issue)
        if not login:
            continue
        ensure_user(metrics, days, login)
        names[login] = login
        if in_range(issue.get("created_at"), dt_from, dt_to):
            metrics[login]["events"] += 1
            metrics[login]["issues_opened"] += 1
            days[login].add(parse_iso(issue["created_at"]).date())
        if in_range(issue.get("closed_at"), dt_from, dt_to):
            metrics[login]["events"] += 1
            metrics[login]["issues_closed"] += 1
            metrics[login]["closures"] += 1
            days[login].add(parse_iso(issue["closed_at"]).date())

    print("Recolectando comentarios...")
    for comment in paged(f"/repos{repo}/issues/comments", token, {"since": since, "sort": "updated", "direction": "desc"}):
        if not in_range(comment.get("created_at"), dt_from, dt_to):
            continue
        login = actor_login(comment)
        if not login:
            continue
        ensure_user(metrics, days, login)
        names[login] = login
        metrics[login]["events"] += 1
        metrics[login]["comments"] += 1
        days[login].add(parse_iso(comment["created_at"]).date())

    print("Recolectando eventos de issues (asignaciones/reaperturas)...")
    for ev in paged(f"/repos{repo}/issues/events", token, {}):
        created = ev.get("created_at")
        if not created:
            continue
        dt = parse_iso(created)
        if dt < dt_from:
            break
        if dt > dt_to:
            continue
        login = actor_login(ev, "actor")
        if not login:
            continue
        ensure_user(metrics, days, login)
        names[login] = login
        event = (ev.get("event") or "").lower()
        metrics[login]["events"] += 1
        days[login].add(dt.date())
        if event in {"assigned", "unassigned"}:
            metrics[login]["assignments"] += 1
        elif event == "closed":
            metrics[login]["closures"] += 1
        elif event == "reopened":
            metrics[login]["reopens"] += 1

    rows: List[Dict[str, Any]] = []
    for login in sorted(metrics.keys()):
        c = metrics[login]
        row = {
            "login": login,
            "name": names.get(login, login),
            "days_active": len(days[login]),
            "events": c.get("events", 0),
            "commits": c.get("commits", 0),
            "prs_opened": c.get("prs_opened", 0),
            "prs_merged": c.get("prs_merged", 0),
            "issues_opened": c.get("issues_opened", 0),
            "issues_closed": c.get("issues_closed", 0),
            "reviews": c.get("reviews", 0),
            "comments": c.get("comments", 0),
            "assignments": c.get("assignments", 0),
            "closures": c.get("closures", 0),
            "reopens": c.get("reopens", 0),
        }
        row["score"] = compute_teacher_score(row)
        row["teacher_level"] = teacher_level(row)
        rows.append(row)

    rows.sort(key=lambda r: (-r["score"], -r["events"], -r["days_active"], r["login"]))

    lines: List[str] = []
    lines.append(f"# Resumen de actividad GitHub - {REPO_OWNER}/{REPO_NAME}")
    lines.append("")
    lines.append(f"Periodo: {DATE_FROM} - {DATE_TO}")
    lines.append("")
    lines.append("## Ranking docente")
    lines.append("")
    lines.append("| Puesto | Alumno | Nivel | Puntuacion | Eventos | Dias activos | Commits | PR abiertas | PR mergeadas | Issues abiertas | Issues cerradas | Reviews | Comentarios | Asign. | Cierres | Reap. |")
    lines.append("|---:|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|")
    for idx, row in enumerate(rows, start=1):
        lines.append(
            f"| {idx} | {row['name']} (`{row['login']}`) | **{row['teacher_level']}** | {row['score']} | {row['events']} | {row['days_active']} | {row['commits']} | {row['prs_opened']} | {row['prs_merged']} | {row['issues_opened']} | {row['issues_closed']} | {row['reviews']} | {row['comments']} | {row['assignments']} | {row['closures']} | {row['reopens']} |"
        )

    lines.append("")
    lines.append("## Criterio docente orientativo")
    lines.append("")
    lines.append("- **Alta**: actividad sostenida y volumen alto de contribucion.")
    lines.append("- **Media**: actividad regular con aportes claros.")
    lines.append("- **Baja**: actividad puntual.")
    lines.append("- **Nula**: sin actividad detectada en el periodo.")

    out_file = script_dir / "github_activity_summary.md"
    out_file.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"Resumen generado: {out_file}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

