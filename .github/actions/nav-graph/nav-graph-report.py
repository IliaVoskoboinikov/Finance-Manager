#!/usr/bin/env python3
"""Рендерит Markdown-отчёт по карте навигации для GitHub Actions job summary.

Читает агрегированные графы, которые оставляют задачи плагина compose-nav-graph:
  * app/build/navgraph-aggregated/nav-graph.json      — экраны и переходы
  * app/build/navgallery-aggregated/preview-gallery.json — все @Preview проекта

Печатает Markdown в stdout; workflow дописывает его в $GITHUB_STEP_SUMMARY.
Пути можно переопределить аргументами: nav-graph-report.py <nav-graph.json> <gallery.json>
"""
import json
import sys
from pathlib import Path

NAV = Path(sys.argv[1] if len(sys.argv) > 1 else "app/build/navgraph-aggregated/nav-graph.json")
GALLERY = Path(sys.argv[2] if len(sys.argv) > 2 else "app/build/navgallery-aggregated/preview-gallery.json")


def load(path):
    try:
        return json.loads(path.read_text())
    except (OSError, ValueError):
        return None


def has_thumb(node):
    return any(p.get("thumbnail") for p in node.get("previews", []))


def main():
    nav = load(NAV)
    if nav is None:
        print(f"> ⚠️ Не найден `{NAV}` — граф не сгенерирован.")
        return

    nodes = sorted(nav.get("nodes", []), key=lambda n: n.get("route", ""))
    edges = nav.get("edges", [])
    with_thumb = sum(1 for n in nodes if has_thumb(n))

    gallery = load(GALLERY) or {}
    previews = sum(len(n.get("previews", [])) for n in gallery.get("nodes", []))
    packages = {
        ".".join((p.get("previewMethodFqn") or p.get("previewFqn") or "").split(".")[:-2])
        for n in gallery.get("nodes", [])
        for p in n.get("previews", [])
    }
    packages.discard("")

    out = []
    out.append("## 🗺️ Карта навигации")
    out.append("")
    out.append(
        f"**{len(nodes)}** экран(ов) · **{len(edges)}** переход(ов) · "
        f"**{with_thumb}/{len(nodes)}** с миниатюрой · "
        f"галерея: **{previews}** `@Preview` в **{len(packages)}** пакет(ах)"
    )
    out.append("")

    out.append("### Экраны")
    out.append("")
    out.append("| Экран | Модуль | Аргументы | Превью |")
    out.append("| --- | --- | --- | --- |")
    for n in nodes:
        route = n.get("route", "?")
        if n.get("start"):
            route += " ★"
        args = ", ".join(a.get("name", "") for a in n.get("args", [])) or "—"
        thumb = "✅" if has_thumb(n) else "—"
        out.append(f"| `{route}` | `{n.get('module', '')}` | {args} | {thumb} |")
    out.append("")

    out.append("### Переходы")
    out.append("")
    out.append("| Из | В | Метка |")
    out.append("| --- | --- | --- |")
    for e in edges:
        frm = e.get("from", "").split(".")[-1]
        to = e.get("to", "").split(".")[-1]
        out.append(f"| `{frm}` | `{to}` | {e.get('label', '')} |")
    out.append("")

    missing = [n.get("route") for n in nodes if not has_thumb(n)]
    if missing:
        out.append(f"> ⚠️ Без миниатюры: {', '.join('`%s`' % m for m in missing)}.")
    out.append(
        "> ℹ️ Splash (Lottie) и круговая диаграмма Analysis (YCharts) в headless-рендере "
        "Layoutlib не отрисовываются — это ограничение движка, не разметки."
    )
    out.append("")
    out.append("📦 Полный отчёт (PNG + интерактивный HTML графа и галереи) — в артефакте прогона.")

    print("\n".join(out))


if __name__ == "__main__":
    main()
