"""Generate SpendSense Cursor demo deck (2 slides + formal speaker notes).

Visual theme: Theme A — Midnight Ledger (see docs/design-wireframes.md and frontend/src/lib/theme.ts).
"""
from pathlib import Path

from pptx import Presentation
from pptx.dml.color import RGBColor
from pptx.enum.shapes import MSO_SHAPE
from pptx.enum.text import MSO_ANCHOR, PP_ALIGN
from pptx.util import Inches, Pt

OUT = Path(__file__).resolve().parent / "SpendSense-Cursor-Demo.pptx"

# Theme A — Midnight Ledger
PAGE_BG = RGBColor(0x0F, 0x17, 0x2A)       # slate-950
SURFACE = RGBColor(0x1E, 0x29, 0x3B)       # slate-900
SURFACE_ALT = RGBColor(0x15, 0x23, 0x3A)   # between page and surface
BORDER = RGBColor(0x33, 0x41, 0x55)        # slate-700
TEXT_PRIMARY = RGBColor(0xF1, 0xF5, 0xF9)  # slate-100
TEXT_MUTED = RGBColor(0x94, 0xA3, 0xB8)    # slate-400
EMERALD = RGBColor(0x22, 0xC5, 0x5E)       # emerald-500
EMERALD_LIGHT = RGBColor(0x34, 0xD3, 0x99) # emerald-400
VIOLET = RGBColor(0xA7, 0x8B, 0xFA)        # violet-400 — AI accent

FONT = "Segoe UI"
SLIDE_W = Inches(13.333)
SLIDE_H = Inches(7.5)
MARGIN = Inches(0.55)


def _solid_rect(slide, left, top, width, height, fill_rgb, line_rgb=None, line_width=Pt(1)):
    shape = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, left, top, width, height)
    shape.fill.solid()
    shape.fill.fore_color.rgb = fill_rgb
    if line_rgb is None:
        shape.line.fill.background()
    else:
        shape.line.color.rgb = line_rgb
        shape.line.width = line_width
    return shape


def _send_to_back(slide, shape) -> None:
    slide.shapes._spTree.remove(shape._element)
    slide.shapes._spTree.insert(2, shape._element)


def apply_midnight_ledger_theme(slide, slide_num: int, total_slides: int = 2) -> None:
    """Paint slide chrome: dark page, header bar, emerald accent, footer rule."""
    bg = _solid_rect(slide, Inches(0), Inches(0), SLIDE_W, SLIDE_H, PAGE_BG)
    _send_to_back(slide, bg)

    header_h = Inches(0.92)
    header = _solid_rect(slide, Inches(0), Inches(0), SLIDE_W, header_h, SURFACE, BORDER)
    _send_to_back(slide, header)

    stripe = _solid_rect(slide, Inches(0), header_h, SLIDE_W, Inches(0.06), EMERALD)
    _send_to_back(slide, stripe)

    logo = slide.shapes.add_textbox(MARGIN, Inches(0.22), Inches(3.2), Inches(0.5))
    lp = logo.text_frame.paragraphs[0]
    lp.text = "SPENDSENSE"
    lp.font.name = FONT
    lp.font.size = Pt(13)
    lp.font.bold = True
    lp.font.color.rgb = EMERALD_LIGHT

    tag = slide.shapes.add_textbox(Inches(3.4), Inches(0.28), Inches(5.5), Inches(0.4))
    tp = tag.text_frame.paragraphs[0]
    tp.text = "Cursor Engineering Demo"
    tp.font.name = FONT
    tp.font.size = Pt(11)
    tp.font.color.rgb = TEXT_MUTED

    counter = slide.shapes.add_textbox(Inches(11.6), Inches(0.26), Inches(1.2), Inches(0.4))
    cp = counter.text_frame.paragraphs[0]
    cp.text = f"{slide_num} / {total_slides}"
    cp.font.name = FONT
    cp.font.size = Pt(11)
    cp.font.color.rgb = TEXT_MUTED
    cp.alignment = PP_ALIGN.RIGHT

    footer_rule = _solid_rect(
        slide, MARGIN, Inches(6.35), SLIDE_W - MARGIN * 2, Inches(0.02), BORDER
    )
    _send_to_back(slide, footer_rule)


def add_content_card(slide, top: float, height: float):
    left = MARGIN
    width = SLIDE_W - MARGIN * 2
    card = _solid_rect(
        slide,
        left,
        Inches(top),
        width,
        Inches(height),
        SURFACE_ALT,
        BORDER,
        Pt(1.25),
    )
    _send_to_back(slide, card)
    return card


def add_title_block(slide, title: str, subtitle: str = "") -> None:
    title_box = slide.shapes.add_textbox(MARGIN, Inches(1.15), Inches(12.2), Inches(1.15))
    tf = title_box.text_frame
    tf.word_wrap = True
    p = tf.paragraphs[0]
    p.text = title
    p.font.name = FONT
    p.font.size = Pt(32)
    p.font.bold = True
    p.font.color.rgb = TEXT_PRIMARY

    if subtitle:
        sub = slide.shapes.add_textbox(MARGIN, Inches(2.05), Inches(12.2), Inches(0.55))
        stf = sub.text_frame
        sp = stf.paragraphs[0]
        sp.text = subtitle
        sp.font.name = FONT
        sp.font.size = Pt(16)
        sp.font.color.rgb = TEXT_MUTED


def add_workflow_pills(slide, steps: list[str]) -> None:
    """Render Plan → Skill → … as emerald-tinted nav pills (matches app active nav)."""
    x = float(MARGIN.inches) + 0.05
    y = 2.62
    for i, step in enumerate(steps):
        pill_w = max(1.35, len(step) * 0.11 + 0.55)
        pill = _solid_rect(
            slide,
            Inches(x),
            Inches(y),
            Inches(pill_w),
            Inches(0.38),
            RGBColor(0x06, 0x4E, 0x3B),  # emerald-500/15 on dark
            EMERALD,
            Pt(0.75),
        )
        pill.shadow.inherit = False

        label = slide.shapes.add_textbox(Inches(x + 0.12), Inches(y + 0.05), Inches(pill_w - 0.2), Inches(0.3))
        lp = label.text_frame.paragraphs[0]
        lp.text = step
        lp.font.name = FONT
        lp.font.size = Pt(11)
        lp.font.bold = True
        lp.font.color.rgb = EMERALD_LIGHT
        lp.alignment = PP_ALIGN.CENTER

        x += pill_w + 0.18
        if i < len(steps) - 1:
            arrow = slide.shapes.add_textbox(Inches(x - 0.12), Inches(y + 0.02), Inches(0.2), Inches(0.3))
            ap = arrow.text_frame.paragraphs[0]
            ap.text = "→"
            ap.font.name = FONT
            ap.font.size = Pt(12)
            ap.font.color.rgb = TEXT_MUTED
            x += 0.08


def add_bullets(slide, items: list[str], top: float = 2.85, size: int = 18) -> None:
    add_content_card(slide, top=top - 0.2, height=3.35)
    body = slide.shapes.add_textbox(
        MARGIN + Inches(0.25), Inches(top), Inches(11.5), Inches(3.1)
    )
    tf = body.text_frame
    tf.word_wrap = True
    tf.vertical_anchor = MSO_ANCHOR.TOP
    for i, item in enumerate(items):
        p = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
        p.space_after = Pt(14)
        p.line_spacing = 1.15
        bullet_run = p.add_run()
        bullet_run.text = "▸  "
        bullet_run.font.name = FONT
        bullet_run.font.size = Pt(size)
        bullet_run.font.color.rgb = EMERALD
        text_run = p.add_run()
        text_run.text = item
        text_run.font.name = FONT
        text_run.font.size = Pt(size)
        text_run.font.color.rgb = TEXT_PRIMARY


def add_footer(slide, text: str) -> None:
    footer = slide.shapes.add_textbox(MARGIN, Inches(6.55), Inches(12.0), Inches(0.55))
    fp = footer.text_frame.paragraphs[0]
    fp.text = text
    fp.font.name = FONT
    fp.font.size = Pt(14)
    fp.font.italic = True
    fp.font.color.rgb = EMERALD_LIGHT


def add_ai_badge(slide) -> None:
    """Small violet AI accent — matches InsightsCards styling in the app."""
    badge = _solid_rect(
        slide,
        Inches(11.15),
        Inches(1.18),
        Inches(1.55),
        Inches(0.34),
        RGBColor(0x2E, 0x1F, 0x47),
        VIOLET,
        Pt(0.75),
    )
    label = slide.shapes.add_textbox(Inches(11.22), Inches(1.22), Inches(1.4), Inches(0.28))
    lp = label.text_frame.paragraphs[0]
    lp.text = "✦ AI Insights"
    lp.font.name = FONT
    lp.font.size = Pt(9)
    lp.font.bold = True
    lp.font.color.rgb = VIOLET
    lp.alignment = PP_ALIGN.CENTER
    _ = badge


def set_notes(slide, text: str) -> None:
    slide.notes_slide.notes_text_frame.text = text


def main() -> None:
    prs = Presentation()
    prs.slide_width = SLIDE_W
    prs.slide_height = SLIDE_H
    blank = prs.slide_layouts[6]

    # --- Slide 1: Opening ---
    s1 = prs.slides.add_slide(blank)
    apply_midnight_ledger_theme(s1, slide_num=1)
    add_ai_badge(s1)
    add_title_block(
        s1,
        "SpendSense — Personal Expense Tracker with AI Insights",
        "Seventeen-Day MVP Sprint  |  Engineering Demonstration  |  10–12 Minutes",
    )
    add_bullets(
        s1,
        [
            "Delivered: JWT authentication, expenses, categories, dashboard, Gemini AI, CSV I/O",
            "Stack: Spring Boot 3 + SQLite  |  React 19 + Vite  |  JUnit + Vitest + Playwright",
            "Session focus: how Cursor accelerated delivery — plans, skills, tests — not UI alone",
            "Agenda: repository tour  →  live demonstration  →  challenges and practices",
        ],
        top=2.75,
        size=18,
    )
    add_footer(s1, "SpendSense is the proof. The Cursor workflow is the story.")

    set_notes(
        s1,
        """SLIDE 1 — OPENING (0:00–1:00)

[ON SCREEN: Slide 1]

SAY:
"Good [morning/afternoon/evening], everyone. Thank you for joining this session.

My name is [Your Name], and today I will walk you through SpendSense — a personal expense tracker with AI-powered insights that we delivered as part of a structured seventeen-day MVP sprint.

SpendSense provides JWT-based authentication, category and expense management, an interactive dashboard with charts, Gemini-powered expense categorisation and spending insights, and CSV import and export. The technology stack comprises Spring Boot 3 with SQLite on the backend, React 19 with Vite on the frontend, and a comprehensive test suite spanning JUnit and MockMvc, Vitest with React Testing Library, and Playwright end-to-end tests.

The purpose of this presentation is not solely to demonstrate the application. Our primary objective is to illustrate how we used Cursor to accelerate delivery while maintaining engineering discipline — through written plans, scoped agent skills, and test-driven completion criteria.

I will begin with our Cursor workflow in the repository, follow with a concise live demonstration of the application, and conclude with the challenges we encountered and the practices that addressed them.

Let me switch to the codebase."

[ACTION: Advance to Cursor. Do not open the application yet.]

DO:
- Deliver greeting and agenda from this slide.
- Transition to Cursor within 60 seconds.

DON'T:
- Enumerate all seventeen sprint days.
- Start the live app demo on this slide.""",
    )

    # --- Slide 2: Closing ---
    s2 = prs.slides.add_slide(blank)
    apply_midnight_ledger_theme(s2, slide_num=2)
    add_title_block(
        s2,
        "Cursor Workflow That Scaled",
        "Plan  →  Skill  →  Audit  →  Implement  →  Verify",
    )
    add_workflow_pills(s2, ["Plan", "Skill", "Audit", "Implement", "Verify"])
    add_bullets(
        s2,
        [
            "Three artefacts: MVP plan (.cursor/plans/) · Agent Skills (.cursor/skills/) · test-cases.md",
            "Challenges overcome: testable AI (MockAiService) · userId isolation (TC-S01) · day-scoped scope",
            "Best practices: audit before implement · TC-IDs wired to real tests · mvnw test + npm test",
            "Outcome: full-stack app with AI in seventeen days — workflow transferable across teams",
        ],
        top=3.15,
        size=18,
    )
    add_footer(s2, "Cursor accelerates execution. Discipline keeps it shippable.")

    set_notes(
        s2,
        """SLIDE 2 — CLOSING (10:30–12:00)

[ON SCREEN: Slide 2]

SAY (after challenges segment, 9:00–10:30):
"Before I conclude, I would like to summarise three challenges we encountered and the practices that resolved them.

First — integrating AI within a testable pipeline. Large language model APIs are non-deterministic and rate-limited. Our solution was MockAiService for the test profile, live Gemini only in dev/prod, and rate limiting in our development startup script.

Second — multi-tenant data isolation. Every repository query is scoped by user identifier — findByIdAndUserId — and security test TC-S01 audits that new endpoints do not expose cross-user data.

Third — scope control across a seventeen-day sprint. The deferred-features list in the MVP plan and day-scoped Agent Skills prevented feature creep. 'Build Day 4 — Expense API' produces substantially better outcomes than 'build an expense tracker.'

To conclude, three artefacts enabled this workflow at scale.

The MVP plan provided architectural and testing context for every agent session. Agent Skills translated sprint days into bounded, repeatable tasks. The test case catalog linked documentation to executable proof.

SpendSense demonstrates that a structured Cursor workflow can deliver a full-stack application with AI integration in seventeen days. The workflow itself — plan, skill, audit, implement, verify — is transferable to other services and codebases within our organisation.

Thank you for your attention. I am happy to take questions on skill authoring, prompt design, test strategy, or adaptation to your team's stack."

[PAUSE for Q&A]

Suggested questions if the room is quiet:
- How do we prevent the agent from modifying Flyway migrations that have already been applied?
- When should we author a new skill versus issuing a one-off prompt?
- What is our recommended review process for agent-generated pull requests?

DO:
- Show this slide during the closing summary.
- Invite questions; do not re-tour repository files.

DON'T:
- Re-open plan.md, SKILL.md, or test-cases.md on this slide.""",
    )

    tmp = OUT.with_suffix(".tmp.pptx")
    prs.save(tmp)
    try:
        tmp.replace(OUT)
    except OSError:
        fallback = OUT.with_name(f"{OUT.stem}-themed{OUT.suffix}")
        tmp.replace(fallback)
        print(f"Wrote {fallback} (close {OUT.name} and re-run to overwrite)")
        return
    print(f"Wrote {OUT}")


if __name__ == "__main__":
    main()
