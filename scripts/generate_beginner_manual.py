# -*- coding: utf-8 -*-
"""Capture gameplay screenshots and build a beginner PDF manual on the Desktop."""
from __future__ import annotations

import os
import re
import shutil
import sys
import tempfile
import time
from dataclasses import dataclass
from pathlib import Path

from PIL import Image
from playwright.sync_api import sync_playwright
from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from reportlab.lib.pagesizes import A4, landscape
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (
    Image as RlImage,
    KeepTogether,
    PageBreak,
    Paragraph,
    SimpleDocTemplate,
    Spacer,
)

ROOT = Path(__file__).resolve().parents[1]
BASE_URL = os.environ.get("NU_MANUAL_BASE_URL", "http://127.0.0.1:8080")
DESKTOP = Path.home() / "Desktop"
OUT_PDF = DESKTOP / "ナインユニバース_初心者向け説明書.pdf"
COVER_ICON = ROOT / "electron" / "build-resources" / "desktop_01.PNG"
VIEWPORT = {"width": 1280, "height": 800}
DEFAULT_PASSWORD = os.environ.get("NU_MANUAL_PASS", "ManualGuide2026!")


def manual_credentials() -> tuple[str, str]:
    user = os.environ.get("NU_MANUAL_USER")
    if not user:
        user = f"nu_manual_{int(time.time())}"
    return user, DEFAULT_PASSWORD

PAGE_SIZE = landscape(A4)

# デッキ一覧のみ（/decks/league/.../edit はマッチさせない）
DECKS_LIST_URL = re.compile(r".*/decks/?$")


def wait_decks_list_page(page, timeout: int = 20000) -> None:
    page.wait_for_url(DECKS_LIST_URL, timeout=timeout)
    page.wait_for_timeout(400)
    err = page.locator(".error")
    if err.count() and err.is_visible():
        raise RuntimeError(err.inner_text())


# 横向きA4に収める画像の上限
IMG_MAX_W_MM = 248
IMG_MAX_H_MM = 118

# 説明書用サンプルカード（狩人・フィールド共通）
MANUAL_CARD_CAPTURE_PX = 280
MANUAL_CARD_PDF_W_MM = 62
MANUAL_CARD_PDF_H_MM = 88
MANUAL_CARD_IMAGE_KEYS = frozenset({"card_karyudo", "card_field_nebula"})


@dataclass(frozen=True)
class ManualPage:
    """1トピック＝1ページ。説明と画像は必ず同じページに収める。"""
    title: str
    lines: tuple[str, ...]
    image_key: str
    caption: str


def register_fonts() -> str:
    candidates = [
        Path(r"C:\Windows\Fonts\meiryo.ttc"),
        Path(r"C:\Windows\Fonts\YuGothM.ttc"),
        Path(r"C:\Windows\Fonts\msgothic.ttc"),
    ]
    for path in candidates:
        if path.exists():
            pdfmetrics.registerFont(TTFont("JaUI", str(path), subfontIndex=0))
            return "JaUI"
    raise RuntimeError("Japanese UI font not found on Windows")


def dismiss_overlays(page) -> None:
    for btn_id in ("announcement-close", "welcome-bonus-close"):
        btn = page.locator(f"#{btn_id}")
        if btn.count() and btn.is_visible():
            btn.click()
            page.wait_for_timeout(400)
    page.evaluate(
        """() => {
          const m = document.getElementById('announcement-modal');
          if (m) { m.hidden = true; m.setAttribute('aria-hidden', 'true'); }
          document.body.style.overflow = '';
        }"""
    )


def try_login(page, username: str, password: str) -> bool:
    page.goto(f"{BASE_URL}/login", wait_until="networkidle")
    page.fill('input[name="username"]', username)
    page.fill('input[name="password"]', password)
    page.click('button[type="submit"]')
    page.wait_for_timeout(1200)
    if "/home" in page.url or page.url.rstrip("/") == BASE_URL:
        return True
    return "/login" not in page.url


def register_account(page, username: str, password: str) -> None:
    page.goto(f"{BASE_URL}/register", wait_until="networkidle")
    page.fill('input[name="username"]', username)
    page.fill('input[name="password"]', password)
    page.click('button[type="submit"]')
    page.wait_for_timeout(1500)


def ensure_logged_in(page) -> tuple[str, str]:
    username, password = manual_credentials()
    if try_login(page, username, password):
        return username, password
    register_account(page, username, password)
    if not try_login(page, username, password):
        raise RuntimeError(f"Could not log in or register user: {username}")
    return username, password


def shot(page, path: Path, url: str | None = None, full_page: bool = True) -> None:
    if url:
        page.goto(url, wait_until="networkidle")
        page.wait_for_timeout(800)
    dismiss_overlays(page)
    page.screenshot(path=str(path), full_page=full_page)


def open_starter_packs(page, max_attempts: int = 8) -> None:
    for _ in range(max_attempts):
        page.goto(f"{BASE_URL}/pack", wait_until="networkidle")
        dismiss_overlays(page)
        open_btn = page.locator('form[action*="/pack/open"] button[type="submit"]')
        if not open_btn.count() or not open_btn.is_enabled():
            break
        open_btn.click()
        page.wait_for_timeout(1800)


def ensure_playable_deck(page) -> None:
    page.goto(f"{BASE_URL}/battle/cpu/casual", wait_until="networkidle")
    page.wait_for_timeout(500)
    if page.locator('form[action*="battle/cpu/start"]').count():
        return

    open_starter_packs(page, 6)

    page.goto(f"{BASE_URL}/decks/new", wait_until="networkidle")
    dismiss_overlays(page)
    page.wait_for_selector("#deck-auto-build-open", timeout=20000)
    page.wait_for_timeout(1200)
    page.locator("#deck-auto-build-open").click()
    page.wait_for_selector("#deck-auto-deck-tribe-grid .deck-auto-deck-tribe-btn", timeout=8000)
    page.locator("#deck-auto-deck-tribe-grid .deck-auto-deck-tribe-btn").first.click()
    page.wait_for_function(
        """() => {
          const t = document.getElementById('deck-count')?.textContent || '';
          const ids = (document.getElementById('cardIds')?.value || '')
            .split(',').filter(Boolean);
          return (t.includes('8 / 8') || t.includes('8/8')) && ids.length === 8;
        }""",
        timeout=15000,
    )
    page.locator('input[name="name"]').fill("説明書用デッキ")
    page.locator("#complete-btn").click(force=True)
    wait_decks_list_page(page)
    page.wait_for_timeout(600)


def extract_first_league_set_id(page) -> str:
    link = page.locator('a[href*="/decks/league/set/"][href*="/slot/1/edit"]').first
    if not link.count():
        return ""
    href = link.get_attribute("href") or ""
    m = re.search(r"/league/set/(\d+)/slot/1", href)
    return m.group(1) if m else ""


def ensure_deck_name_filled(page, default_name: str) -> None:
    name_input = page.locator('input[name="name"]')
    if not name_input.count():
        return
    if not (name_input.input_value() or "").strip():
        name_input.fill(default_name)


def fill_league_slot_greedy(page, set_id: str, slot: int) -> None:
    """おまかせで組めないとき、ライブラリから重複しないカードを8枚選ぶ。"""
    page.goto(f"{BASE_URL}/decks/league/set/{set_id}/slot/{slot}/edit", wait_until="networkidle")
    dismiss_overlays(page)
    page.wait_for_selector("#lib-zone", timeout=20000)
    page.wait_for_timeout(800)
    if page.locator("#deck-clear-all-btn").is_enabled():
        page.locator("#deck-clear-all-btn").click()
        page.wait_for_timeout(400)
    cards = page.locator("#lib-zone .mini-card:not(.mini-card--disabled)")
    for i in range(cards.count()):
        if "8 / 8" in (page.locator("#deck-count").inner_text() or ""):
            break
        cards.nth(i).click()
        page.wait_for_timeout(120)
    page.wait_for_function(
        """() => {
          const t = document.getElementById('deck-count')?.textContent || '';
          const ids = (document.getElementById('cardIds')?.value || '')
            .split(',').filter(Boolean);
          if (!(t.includes('8 / 8') || t.includes('8/8')) || ids.length !== 8) return false;
          const raw = document.querySelector('meta[name="nu_league_blocked_card_ids"]')
            ?.getAttribute('content') || '';
          const blocked = new Set(raw.split(',').map(function (x) {
            const n = parseInt(x.trim(), 10);
            return isNaN(n) ? null : n;
          }).filter(function (n) { return n != null; }));
          if (!blocked.size) return true;
          return !ids.some(function (id) { return blocked.has(parseInt(id, 10)); });
        }""",
        timeout=5000,
    )
    ensure_deck_name_filled(page, f"リーグデッキ{slot}")
    page.locator("#complete-btn").click(force=True)
    wait_decks_list_page(page)


def fill_league_slot_auto(page, set_id: str, slot: int, tribe_idx: int) -> None:
    page.goto(f"{BASE_URL}/decks/league/set/{set_id}/slot/{slot}/edit", wait_until="networkidle")
    dismiss_overlays(page)
    page.wait_for_selector("#deck-auto-build-open", timeout=20000)
    page.wait_for_timeout(1200)
    page.locator("#deck-auto-build-open").click()
    page.wait_for_selector(".deck-auto-deck-tribe-btn", timeout=8000)
    page.locator(".deck-auto-deck-tribe-btn").nth(tribe_idx).click()
    page.wait_for_function(
        """() => {
          const t = document.getElementById('deck-count')?.textContent || '';
          const ids = (document.getElementById('cardIds')?.value || '')
            .split(',').filter(Boolean);
          if (!(t.includes('8 / 8') || t.includes('8/8')) || ids.length !== 8) return false;
          const raw = document.querySelector('meta[name="nu_league_blocked_card_ids"]')
            ?.getAttribute('content') || '';
          const blocked = new Set(raw.split(',').map(function (x) {
            const n = parseInt(x.trim(), 10);
            return isNaN(n) ? null : n;
          }).filter(function (n) { return n != null; }));
          if (!blocked.size) return true;
          return !ids.some(function (id) { return blocked.has(parseInt(id, 10)); });
        }""",
        timeout=15000,
    )
    ensure_deck_name_filled(page, f"リーグデッキ{slot}")
    page.locator("#complete-btn").click(force=True)
    wait_decks_list_page(page)


def ensure_league_set_ready(page) -> str:
    open_starter_packs(page, 12)
    page.goto(f"{BASE_URL}/decks", wait_until="networkidle")
    dismiss_overlays(page)
    set_id = extract_first_league_set_id(page)
    if not set_id:
        page.locator('form[action*="/decks/league/set/new"] input[name="name"]').fill(
            "説明書用リーグ"
        )
        page.locator('form[action*="/decks/league/set/new"] button[type="submit"]').click()
        wait_decks_list_page(page)
        set_id = extract_first_league_set_id(page)
    if not set_id:
        raise RuntimeError("Could not create league deck set")

    for slot in (1, 2):
        filled = False
        tribe_start = 0 if slot == 1 else 1
        for tribe_idx in list(range(tribe_start, 9)) + list(range(0, tribe_start)):
            try:
                fill_league_slot_auto(page, set_id, slot, tribe_idx)
                filled = True
                break
            except Exception:
                page.wait_for_timeout(300)
        if not filled:
            try:
                fill_league_slot_greedy(page, set_id, slot)
                filled = True
            except Exception:
                pass
        if not filled:
            raise RuntimeError(f"Could not fill league deck slot {slot}")
    return set_id


def start_cpu_league_battle(page, set_id: str) -> None:
    page.goto(f"{BASE_URL}/battle/cpu/league", wait_until="networkidle")
    page.wait_for_timeout(600)
    form = page.locator('form[action*="battle/cpu/league/start"]').first
    if form.count() == 0:
        raise RuntimeError("No CPU league battle start form")
    form.locator('select[name="leagueSetId"]').select_option(value=set_id)
    form.locator('select[name="humanDeckSlot"]').select_option("1")
    form.locator('select[name="level"]').select_option("1")
    with page.expect_navigation(timeout=30000):
        form.locator('button[type="submit"]').click()
    if "/battle/cpu/play" not in page.url:
        err = page.locator(".error")
        msg = err.inner_text() if err.count() else page.url
        raise RuntimeError(f"League CPU battle did not start: {msg}")


def simulate_league_battle_screenshot(page, tmp: Path) -> Path:
    """リーグCPU戦を開始し、対戦中の画面を撮影する。"""
    out = tmp / "battle_league.png"
    set_id = ensure_league_set_ready(page)
    start_cpu_league_battle(page, set_id)
    page.wait_for_selector("#battle-app", timeout=45000)

    captured = False
    try:
        page.wait_for_selector(".battle-intro-overlay__league-wins", timeout=16000)
        page.wait_for_timeout(1500)
        page.screenshot(path=str(out))
        captured = True
    except Exception:
        pass

    if not captured:
        wait_battle_ui_ready(page)
        if deploy_via_ui(page):
            refresh_battle_play_page(page)
            page.wait_for_timeout(800)
        screenshot_battle_app(page, out)

    return out


def start_cpu_battle(page) -> None:
    ensure_playable_deck(page)
    page.goto(f"{BASE_URL}/battle/cpu/casual", wait_until="networkidle")
    page.wait_for_timeout(600)
    form = page.locator('form[action*="battle/cpu/start"]').first
    if form.count() == 0:
        raise RuntimeError("No CPU battle start form after deck setup")
    form.locator('select[name="level"]').select_option("1")
    form.locator('button[type="submit"]').click()
    page.wait_for_url("**/battle/cpu/play**", timeout=20000)


def wait_battle_ui_ready(page, timeout_ms: int = 45000) -> None:
    page.wait_for_selector("#battle-app", timeout=timeout_ms)
    page.wait_for_function(
        """() => {
          const app = document.getElementById('battle-app');
          if (!app) return false;
          const t = app.textContent || '';
          return !t.includes('読み込み中') && !t.includes('読み込みに失敗');
        }""",
        timeout=timeout_ms,
    )
    page.wait_for_function(
        "() => !document.querySelector('.battle-intro-overlay')",
        timeout=20000,
    )
    page.wait_for_timeout(600)


def screenshot_battle_app(page, path: Path) -> None:
    page.evaluate(
        """() => {
          document.querySelectorAll(
            '.battle-pay-modal, .battle-intro-overlay, #battle-pending-choice-modal'
          ).forEach((el) => el.remove());
        }"""
    )
    page.locator("#battle-app").screenshot(path=str(path))


def capture_level_up_screenshot(page, path: Path) -> bool:
    """レベルアップポップアップを開いた状態で撮影（配置は確定しない）。"""
    hand = page.locator("button.hand-card.battle-card:not([disabled])")
    if hand.count() == 0:
        return False
    hand.first.click()
    page.wait_for_timeout(500)
    overlay = page.locator(".battle-control-overlay--levelup-popup")
    if not overlay.count() or not overlay.is_visible():
        return False
    stone_plus = page.locator('[data-action="stone_plus"]')
    if stone_plus.count() and stone_plus.is_visible():
        stone_plus.click()
        page.wait_for_timeout(350)
    page.locator("#battle-app").screenshot(path=str(path))
    cancel = page.locator(".battle-control__cancel-external")
    if cancel.count() and cancel.is_visible():
        cancel.click()
        page.wait_for_timeout(300)
    return True


def battle_fetch_state(page) -> dict:
    return page.evaluate(
        """async () => {
          const csrf = document.querySelector('meta[name="_csrf"]')?.content;
          const hdr = document.querySelector('meta[name="_csrf_header"]')?.content;
          const h = { Accept: 'application/json' };
          if (csrf && hdr) h[hdr] = csrf;
          const cp = document.querySelector('meta[name="nine_universe_context_path"]')?.content || '';
          const r = await fetch(cp + '/battle/cpu/state', { headers: h });
          if (!r.ok) throw new Error('state ' + r.status);
          return r.json();
        }"""
    )


def battle_api_post(page, path: str, body: dict | None = None) -> dict:
    return page.evaluate(
        """async ({ path, body }) => {
          const csrf = document.querySelector('meta[name="_csrf"]')?.content;
          const hdr = document.querySelector('meta[name="_csrf_header"]')?.content;
          const h = { Accept: 'application/json' };
          if (csrf && hdr) h[hdr] = csrf;
          if (body != null) h['Content-Type'] = 'application/json';
          const cp = document.querySelector('meta[name="nine_universe_context_path"]')?.content || '';
          const r = await fetch(cp + path, {
            method: 'POST',
            headers: h,
            body: body != null ? JSON.stringify(body) : undefined,
          });
          if (!r.ok) throw new Error(path + ' ' + r.status);
          return r.json();
        }""",
        {"path": path, "body": body},
    )


def refresh_battle_play_page(page) -> None:
    page.goto(f"{BASE_URL}/battle/cpu/play", wait_until="domcontentloaded")
    wait_battle_ui_ready(page)


def confirm_pay_modal(page) -> None:
    pay = page.locator(".battle-pay-modal")
    if not pay.count() or not pay.is_visible():
        return
    decide_btn = pay.locator('button.btn--primary:has-text("決定")')
    plus = pay.locator('button:has-text("+")').first
    pay_cards = pay.locator(".battle-pay-modal__card")
    for _ in range(16):
        if decide_btn.count() and decide_btn.is_enabled():
            break
        if plus.count() and plus.is_enabled():
            plus.click()
            page.wait_for_timeout(100)
            continue
        clicked = False
        for i in range(pay_cards.count()):
            pay_cards.nth(i).click()
            page.wait_for_timeout(100)
            if decide_btn.count() and decide_btn.is_enabled():
                clicked = True
                break
        if not clicked and decide_btn.count() and decide_btn.is_enabled():
            break
        page.wait_for_timeout(150)
    if decide_btn.count() and decide_btn.is_enabled():
        decide_btn.click()
        page.wait_for_timeout(500)


def deploy_via_ui(page) -> bool:
    hand = page.locator("button.hand-card.battle-card:not([disabled])")
    if hand.count() == 0:
        return False
    hand.first.click()
    page.wait_for_timeout(350)
    decide = page.locator(".battle-control__decide-external")
    if decide.count() and decide.is_visible():
        decide.click()
        page.wait_for_timeout(450)
    confirm_pay_modal(page)
    page.wait_for_timeout(3200)
    return True


def handle_pending_modals(page) -> None:
    choice = page.locator("#battle-pending-choice-modal")
    if choice.count() and choice.is_visible():
        btn = choice.locator("button.btn--primary, button.battle-pay-modal__card").first
        if btn.count():
            btn.click()
            page.wait_for_timeout(800)
    pay = page.locator('.battle-pay-modal button.btn--primary:has-text("決定")')
    if pay.count() and pay.is_visible():
        pay.click()
        page.wait_for_timeout(800)


def advance_battle_one_step(page) -> str:
    """1手分進める。戻り値: done / human / cpu / effect / choice / stuck"""
    st = battle_fetch_state(page)
    if st.get("gameOver"):
        return "done"
    phase = st.get("phase") or ""
    if phase == "HUMAN_CHOICE" and st.get("pendingChoice"):
        handle_pending_modals(page)
        return "choice"
    if phase in ("HUMAN_EFFECT_PENDING", "CPU_EFFECT_PENDING"):
        page.wait_for_timeout(3200)
        try:
            battle_api_post(page, "/battle/cpu/resolve")
        except Exception:
            pass
        refresh_battle_play_page(page)
        return "effect"
    if phase == "CPU_THINKING":
        page.wait_for_timeout(1200)
        try:
            battle_api_post(page, "/battle/cpu/cpu-step")
        except Exception:
            pass
        refresh_battle_play_page(page)
        return "cpu"
    if phase == "HUMAN_INPUT" and st.get("humansTurn"):
        if deploy_via_ui(page):
            refresh_battle_play_page(page)
            return "human"
        return "stuck"
    return "wait"


def simulate_battle_screenshots(page, tmp: Path) -> dict[str, Path]:
    """CPU戦を実際に進行させ、バトル関連ページ用の画像を撮る。"""
    out = {
        "battle": tmp / "battle.png",
        "battle_turn": tmp / "battle_turn.png",
        "battle_levelup": tmp / "battle_levelup.png",
        "battle_deploy": tmp / "battle_deploy.png",
        "battle_mid": tmp / "battle_mid.png",
        "battle_game_summary": tmp / "battle_game_summary.png",
        "battle_cpu": tmp / "battle_cpu.png",
        "battle_unwinnable": tmp / "battle_unwinnable.png",
    }

    start_cpu_battle(page)
    wait_battle_ui_ready(page)

    try:
        page.wait_for_selector("#battle-turn-popup", timeout=8000)
        page.wait_for_timeout(400)
    except Exception:
        pass
    screenshot_battle_app(page, out["battle_turn"])

    screenshot_battle_app(page, out["battle"])

    if not capture_level_up_screenshot(page, out["battle_levelup"]):
        shutil.copy(out["battle"], out["battle_levelup"])

    if deploy_via_ui(page):
        refresh_battle_play_page(page)
        screenshot_battle_app(page, out["battle_deploy"])

    cpu_on_zone = False
    saw_cpu_thinking = False
    saw_unwinnable = False

    for _ in range(48):
        st = battle_fetch_state(page)
        if st.get("gameOver"):
            break
        if st.get("cpuBattle") and st["cpuBattle"].get("main"):
            cpu_on_zone = True
        if st.get("phase") == "CPU_THINKING":
            saw_cpu_thinking = True
        if st.get("noLegalDeploy") and st.get("humansTurn"):
            saw_unwinnable = True

        if cpu_on_zone and not out["battle_mid"].exists():
            screenshot_battle_app(page, out["battle_mid"])
        if cpu_on_zone and not out["battle_game_summary"].exists():
            screenshot_battle_app(page, out["battle_game_summary"])

        if saw_cpu_thinking and not out["battle_cpu"].exists():
            page.wait_for_selector("text=考え中", timeout=3000)
            screenshot_battle_app(page, out["battle_cpu"])

        if saw_unwinnable:
            page.wait_for_timeout(2200)
            pop = page.locator(".battle-unwinnable-pop")
            if pop.count() and pop.is_visible():
                screenshot_battle_app(page, out["battle_unwinnable"])
                break

        step = advance_battle_one_step(page)
        if step == "done":
            break
        if step == "stuck":
            if cpu_on_zone:
                break
            page.wait_for_timeout(800)

    if not out["battle_mid"].exists():
        screenshot_battle_app(page, out["battle_mid"])
    if not out["battle_game_summary"].exists():
        if out["battle_mid"].exists():
            shutil.copy(out["battle_mid"], out["battle_game_summary"])
        elif out["battle_deploy"].exists():
            shutil.copy(out["battle_deploy"], out["battle_game_summary"])
        elif out["battle"].exists():
            shutil.copy(out["battle"], out["battle_game_summary"])
    if not out["battle_unwinnable"].exists():
        if out["battle_mid"].exists():
            shutil.copy(out["battle_mid"], out["battle_unwinnable"])
        else:
            screenshot_battle_app(page, out["battle_unwinnable"])
    if not out["battle_deploy"].exists() and out["battle"].exists():
        shutil.copy(out["battle"], out["battle_deploy"])

    return out


def fit_image(path: Path, max_w_mm: float = IMG_MAX_W_MM, max_h_mm: float = IMG_MAX_H_MM) -> RlImage:
    with Image.open(path) as im:
        w_px, h_px = im.size
    max_w = max_w_mm * mm
    max_h = max_h_mm * mm
    ratio = min(max_w / w_px, max_h / h_px)
    return RlImage(str(path), width=w_px * ratio, height=h_px * ratio)


COVER_GAME_SUMMARY = (
    "カードを集めて、8枚のデッキを作ります。",
    "相手と交互にターンを進め、場にファイターを出します。",
    "相手が出せなくなったら、あなたの勝ちです。",
)


def manual_pages() -> list[ManualPage]:
    return [
        ManualPage(
            "① はじめて遊ぶ方（新規登録・ログイン）",
            (
                "初回は「新規登録」でユーザーIDとパスワードを決めて作成します。",
                "2回目以降はログイン画面から、同じIDで入れます。",
            ),
            image_key="register",
            caption="▲ 新規登録画面",
        ),
        ManualPage(
            "② ホーム画面",
            (
                "ここがメインメニューです。",
                "パック購入・CPU戦・デッキ作成・ライブラリなどへ進めます。",
                "上の数字は、いま持っているジェム（通貨）です。",
            ),
            image_key="home",
            caption="▲ ホーム画面",
        ),
        ManualPage(
            "③ カードパックでカードを集める",
            (
                "ジェムを使ってパックを開くと、新しいカードが手に入ります。",
                "まずはパックを開いて、デッキに使うカードを増やしましょう。",
            ),
            image_key="pack",
            caption="▲ カードパック画面",
        ),
        ManualPage(
            "④ デッキは8枚",
            (
                "バトルに使うデッキは、必ず8枚です。",
                "同じカード名は、2枚まで入れられます。",
            ),
            image_key="decks",
            caption="▲ デッキ一覧",
        ),
        ManualPage(
            "⑤ デッキを編集する",
            (
                "「デッキを作成」から、所持カードを8枚選びます。",
                "CPU戦や対人戦の前に、使うデッキを選びます。",
            ),
            image_key="deck_edit",
            caption="▲ デッキ編集画面",
        ),
        ManualPage(
            "⑥ CPU戦で練習する",
            (
                "ホームの「CPUとバトル」→「カジュアルCPU戦」を選びます。",
                "デッキとCPUの強さ（レベル）を選んで「バトル開始」です。",
            ),
            image_key="cpu_menu",
            caption="▲ CPU戦の準備画面",
        ),
        ManualPage(
            "⑦ バトル画面の見方",
            (
                "中央がバトルゾーン。手札は下、相手の場は上です。",
                "左のストーンは、カードを出すときの支払いに使います。",
            ),
            image_key="battle",
            caption="▲ バトル開始直後の画面（実際のCPU戦）",
        ),
        ManualPage(
            "⑧ 1ターンの流れ",
            (
                "① 自分のターンが始まる（ストーンが1つ増える※）",
                "② 手札のファイターを選び、必要ならレベルアップしてから配置する",
                "③ コストを支払ってバトルゾーンに出す → 相手のターンへ",
                "※ 先攻だけ、最初のターンはストーンが増えません。",
            ),
            image_key="battle_turn",
            caption="▲ 「あなたのターン」表示（実際のCPU戦）",
        ),
        ManualPage(
            "⑨ レベルアップ（強さを上げて出す）",
            (
                "手札のファイターをクリックすると「レベルアップ」画面が開きます。",
                "「カードを捨ててレベルアップ」… 手札をレストに捨てるほど、出すときの強さが+2ずつ上がります。",
                "「ストーンを使ってレベルアップ」… ストーン1つにつき、出すときの強さが+2上がります。",
                "レベルアップは任意です。強さを上げたあと「決定」→ コスト支払いで配置します。",
                "※ 〈フィールド〉カードにはレベルアップはありません。",
            ),
            image_key="battle_levelup",
            caption="▲ レベルアップ画面（実際のCPU戦）",
        ),
        ManualPage(
            "⑩ 勝ち方・負け方",
            (
                "勝ち：相手が、これ以上ファイターを出せなくなったとき",
                "負け：あなたが、これ以上ファイターを出せなくなったとき",
                "出せるカードがないときは「この手番では勝てません」と表示されます。",
            ),
            image_key="battle_unwinnable",
            caption="▲ 配置できない状態（実際のCPU戦）",
        ),
        ManualPage(
            "⑪ 強さのルール（いちばん大事）",
            (
                "相手のバトルゾーンにファイターがいるとき、",
                "あなたが出すファイターは「効果を含めた強さ」が、",
                "相手のファイター以上でないと出せません。",
                "例：相手が強さ5 → あなたは5以上が必要",
            ),
            image_key="battle_mid",
            caption="▲ 相手のファイターがいる状態（実際のCPU戦）",
        ),
        ManualPage(
            "⑫ 〈フィールド〉カード",
            (
                "ファイターとは別のカードで、場全体に効果があります。",
                "出すときはストーンだけで支払います（手札は使えません）。",
                "出したあとも、同じターンにファイターを出せます。",
            ),
            image_key="card_field_nebula",
            caption="▲ サンプルカード：探鉱の洞窟 ネビュラ坑道",
        ),
        ManualPage(
            "⑬ カードの数字・文字の見方",
            (
                "左上の数字 … コスト（出すために必要な値）",
                "カード上の数字 … 強さ",
                "下のテキスト … 効果（出したとき・常時など）",
                "最下のアルファベット … レア度（C→R→Ep→Reg）",
            ),
            image_key="card_karyudo",
            caption="▲ サンプルカード：狩人",
        ),
        ManualPage(
            "⑭ リーグ対戦",
            (
                "リーグデッキはデッキ1・デッキ2の2セット（各8枚）です。",
                "同じカード名は、2つのデッキの間では重複できません（デッキ作成画面で登録）。",
                "マッチは2本先取 … 先に2ゲーム取った方が勝ちです。",
                "各ゲームの勝者は、次のゲームで必ずもう片方のデッキに切り替わります。",
                "CPUは「ひとりで対戦」→「リーグCPU戦」、対人は「だれかと対戦」→「リーグ対戦」です。",
            ),
            image_key="battle_league",
            caption="▲ リーグ対戦中の画面（CPU戦を実際に進行）",
        ),
        ManualPage(
            "⑮ 慣れたら",
            (
                "「フレンド」で友だちを登録し、「だれかと対戦」へ。",
                "「ミッション」をこなすとジェムがもらえます。",
                "わからないときは、ホーム右下の「遊び方」を開いてください。",
            ),
            image_key="friends",
            caption="▲ フレンド画面",
        ),
    ]


def cover_page_spec() -> ManualPage:
    return ManualPage(
        "はじめての方へ",
        (
            "カードを集めてデッキを作り、交互にファイターを出して勝ち抜くゲームです。",
            "この説明書は、画面の流れに沿って1ページずつ説明します。",
        ),
        image_key="",
        caption="",
    )


def game_summary_page_spec() -> ManualPage:
    return ManualPage(
        "このゲームですること",
        COVER_GAME_SUMMARY,
        image_key="battle_game_summary",
        caption="▲ バトル画面（CPU戦を実際に進行した様子）",
    )


def build_pdf(shots: dict[str, Path], font: str) -> None:
    styles = getSampleStyleSheet()
    title = ParagraphStyle(
        "Title",
        parent=styles["Title"],
        fontName=font,
        fontSize=24,
        leading=30,
        alignment=TA_CENTER,
        textColor=colors.HexColor("#1a2744"),
        spaceAfter=8,
    )
    subtitle = ParagraphStyle(
        "Subtitle",
        parent=styles["Normal"],
        fontName=font,
        fontSize=13,
        leading=18,
        alignment=TA_CENTER,
        textColor=colors.HexColor("#4a5568"),
        spaceAfter=14,
    )
    page_title = ParagraphStyle(
        "PageTitle",
        parent=styles["Heading1"],
        fontName=font,
        fontSize=15,
        leading=20,
        textColor=colors.white,
        backColor=colors.HexColor("#2c4a7c"),
        borderPadding=6,
        spaceAfter=10,
    )
    body = ParagraphStyle(
        "Body",
        parent=styles["Normal"],
        fontName=font,
        fontSize=11,
        leading=17,
        alignment=TA_LEFT,
        spaceAfter=6,
    )
    step = ParagraphStyle(
        "Step",
        parent=body,
        fontSize=11,
        leading=17,
        leftIndent=4,
        spaceAfter=5,
    )
    caption = ParagraphStyle(
        "Caption",
        parent=body,
        fontSize=9.5,
        textColor=colors.HexColor("#5a6478"),
        alignment=TA_CENTER,
        spaceBefore=4,
        spaceAfter=0,
    )
    doc = SimpleDocTemplate(
        str(OUT_PDF),
        pagesize=PAGE_SIZE,
        leftMargin=16 * mm,
        rightMargin=16 * mm,
        topMargin=14 * mm,
        bottomMargin=14 * mm,
        title="ナインユニバース 初心者向け説明書",
        author="Nine Universe",
    )
    story: list = []

    def append_page(block_title: str, spec: ManualPage, *, is_cover: bool = False) -> None:
        block: list = []
        if is_cover:
            if COVER_ICON.exists():
                block.append(Spacer(1, 8 * mm))
                icon = fit_image(COVER_ICON, max_w_mm=48, max_h_mm=48)
                icon.hAlign = "CENTER"
                block.append(icon)
                block.append(Spacer(1, 4 * mm))
            block.append(Paragraph("ナインユニバース", title))
            block.append(Paragraph(block_title, subtitle))
        else:
            block.append(Paragraph(block_title, page_title))
        block.append(Spacer(1, 3 * mm))
        for line in spec.lines:
            if line.startswith("※"):
                block.append(
                    Paragraph(
                        line,
                        ParagraphStyle("Note", parent=body, fontSize=9.5, textColor=colors.grey),
                    )
                )
            elif re.match(r"^[①②③④⑤⑥⑦⑧⑨⑩\d]", line) or " … " in line:
                block.append(Paragraph(line, step))
            else:
                prefix = "" if is_cover else "・"
                block.append(Paragraph(f"{prefix}{line}", step))
        key = spec.image_key
        if not key:
            story.append(KeepTogether(block))
            story.append(PageBreak())
            return
        if key not in shots or not shots[key].exists():
            raise FileNotFoundError(f"Missing screenshot for page '{block_title}': {key}")
        block.append(Spacer(1, 4 * mm))
        if key in MANUAL_CARD_IMAGE_KEYS:
            img = fit_image(
                shots[key],
                max_w_mm=MANUAL_CARD_PDF_W_MM,
                max_h_mm=MANUAL_CARD_PDF_H_MM,
            )
        else:
            max_h = 95 if len(spec.lines) >= 4 else IMG_MAX_H_MM
            img = fit_image(shots[key], max_h_mm=max_h)
        img.hAlign = "CENTER"
        block.append(img)
        block.append(Paragraph(spec.caption, caption))
        story.append(KeepTogether(block))
        story.append(PageBreak())

    def append_cover_page(spec: ManualPage) -> None:
        block: list = []
        if COVER_ICON.exists():
            block.append(Spacer(1, 6 * mm))
            icon = fit_image(COVER_ICON, max_w_mm=44, max_h_mm=44)
            icon.hAlign = "CENTER"
            block.append(icon)
            block.append(Spacer(1, 3 * mm))
        block.append(Paragraph("ナインユニバース", title))
        block.append(Paragraph(spec.title, subtitle))
        block.append(Spacer(1, 2 * mm))
        for line in spec.lines:
            block.append(
                Paragraph(
                    line,
                    ParagraphStyle(
                        "CoverIntro",
                        parent=body,
                        alignment=TA_CENTER,
                        fontSize=10.5,
                        leading=16,
                    ),
                )
            )
        story.append(KeepTogether(block))
        story.append(PageBreak())

    append_cover_page(cover_page_spec())
    summary_spec = game_summary_page_spec()
    append_page(summary_spec.title, summary_spec)
    for page_spec in manual_pages():
        append_page(page_spec.title, page_spec)

    doc.build(story)


def ensure_nebula_card_image_file() -> None:
    """DB の image_file（スペース区切り）と実ファイル名の差を吸収する。"""
    cards_dir = ROOT / "src" / "main" / "resources" / "static" / "images" / "cards"
    underscore = cards_dir / "探鉱の洞窟_ネビュラ坑道.PNG"
    spaced = cards_dir / "探鉱の洞窟 ネビュラ坑道.PNG"
    if underscore.exists() and not spaced.exists():
        shutil.copy2(underscore, spaced)


def prepare_manual_card_page(page) -> None:
    """遊び方ページを開き、サンプルカード用の figure が2つあることを確認する。"""
    page.goto(f"{BASE_URL}/how-to-play", wait_until="networkidle")
    dismiss_overlays(page)
    page.wait_for_selector(".how-to-play__manual-card-figure", timeout=20000)
    if page.locator(".how-to-play__manual-card-figure").count() < 2:
        raise RuntimeError("Expected 2 manual card figures on how-to-play page")


def eager_load_manual_card_art(page, index: int) -> None:
    """画面外の lazy 画像を読み込み、イラスト層が表示されるまで待つ。"""
    figure = page.locator(".how-to-play__manual-card-figure").nth(index)
    width_px = str(MANUAL_CARD_CAPTURE_PX)
    figure.scroll_into_view_if_needed()
    figure.evaluate(
        """(fig, w) => {
          fig.style.width = 'max-content';
          fig.style.display = 'inline-block';
          const art = fig.querySelector('.how-to-play__card-art');
          const cap = fig.querySelector('.how-to-play__card-caption');
          if (art) {
            art.style.width = w + 'px';
            art.style.maxWidth = 'none';
          }
          if (cap) cap.style.maxWidth = w + 'px';
          const col = fig.closest('.how-to-play__card-col--art');
          if (col) col.style.minWidth = 'max-content';
          fig.querySelectorAll('img').forEach((img) => {
            img.loading = 'eager';
          });
          const portrait = fig.querySelector('.card-face__layer-img--portrait');
          if (portrait) {
            const alt = portrait.getAttribute('data-portrait-alt');
            const src = portrait.getAttribute('src');
            if (src) portrait.src = src;
            if (alt && (!portrait.complete || portrait.naturalWidth === 0)) {
              portrait.src = alt;
            }
          }
          const face = fig.querySelector('.card-face.card-face--layered');
          const nameEl = face && face.querySelector('.card-face__name');
          if (nameEl && nameEl.dataset) {
            delete nameEl.dataset.nameFitDone;
            delete nameEl.dataset.nameFitRetries;
          }
          if (face && typeof window.fitCardFaceNameToOneLine === 'function') {
            window.fitCardFaceNameToOneLine(face);
          }
          if (nameEl && nameEl.clientWidth > 0) {
            let px = parseFloat(window.getComputedStyle(nameEl).fontSize) || 12;
            for (let i = 0; i < 140; i++) {
              if (nameEl.scrollWidth <= nameEl.clientWidth + 0.5) break;
              px = Math.max(5, px - 0.35);
              nameEl.style.fontSize = px + 'px';
            }
            if (nameEl.scrollWidth > nameEl.clientWidth + 0.5) {
              nameEl.style.letterSpacing = '-0.06em';
              for (let i = 0; i < 100; i++) {
                if (nameEl.scrollWidth <= nameEl.clientWidth + 0.5) break;
                px = Math.max(5, px - 0.35);
                nameEl.style.fontSize = px + 'px';
              }
            }
          }
        }""",
        width_px,
    )
    page.wait_for_function(
        """(idx) => {
          const fig = document.querySelectorAll('.how-to-play__manual-card-figure')[idx];
          if (!fig) return false;
          const portrait = fig.querySelector('.card-face__layer-img--portrait');
          const base = fig.querySelector('.card-face__layer-img--base');
          const okImg = (img) =>
            img && !img.hidden && img.complete && img.naturalWidth > 0;
          if (!okImg(portrait) && !okImg(base)) return false;
          const nameEl = fig.querySelector('.card-face__name');
          if (!nameEl) return true;
          const text = (nameEl.textContent || '').trim();
          if (!text) return true;
          return nameEl.clientWidth > 0
            && nameEl.scrollWidth <= nameEl.clientWidth + 1;
        }""",
        arg=index,
        timeout=30000,
    )
    figure.evaluate(
        """async (fig) => {
          if (document.fonts && document.fonts.ready) {
            await document.fonts.ready;
          }
          const face = fig.querySelector('.card-face.card-face--layered');
          const nameEl = face && face.querySelector('.card-face__name');
          if (!nameEl || nameEl.clientWidth <= 0) return;
          if (nameEl.dataset) delete nameEl.dataset.nameFitDone;
          if (typeof window.fitCardFaceNameToOneLine === 'function') {
            window.fitCardFaceNameToOneLine(face);
          }
          let px = parseFloat(window.getComputedStyle(nameEl).fontSize) || 12;
          for (let i = 0; i < 140; i++) {
            if (nameEl.scrollWidth <= nameEl.clientWidth + 0.5) break;
            px = Math.max(5, px - 0.35);
            nameEl.style.fontSize = px + 'px';
          }
        }"""
    )
    page.wait_for_function(
        """(idx) => {
          const fig = document.querySelectorAll('.how-to-play__manual-card-figure')[idx];
          const nameEl = fig && fig.querySelector('.card-face__name');
          if (!nameEl) return true;
          const text = (nameEl.textContent || '').trim();
          if (!text) return true;
          return nameEl.clientWidth > 0
            && nameEl.scrollWidth <= nameEl.clientWidth + 1;
        }""",
        arg=index,
        timeout=15000,
    )
    page.wait_for_timeout(500)


def capture_manual_card_art(figure, out: Path) -> Path:
    """カード面のみ撮影（キャプションは PDF 側の文言を使う）。"""
    art = figure.locator(".how-to-play__card-art")
    if not art.count():
        raise RuntimeError("Card art element missing in manual card figure")
    art.screenshot(path=str(out))
    return out


def capture_manual_card_screenshots(page, tmp: Path) -> tuple[Path, Path]:
    ensure_nebula_card_image_file()
    prepare_manual_card_page(page)
    figures = page.locator(".how-to-play__manual-card-figure")
    eager_load_manual_card_art(page, 0)
    karyudo = capture_manual_card_art(figures.nth(0), tmp / "card_karyudo.png")
    eager_load_manual_card_art(page, 1)
    nebula = capture_manual_card_art(figures.nth(1), tmp / "card_field_nebula.png")
    return karyudo, nebula


def capture_screenshots(tmp: Path) -> dict[str, Path]:
    shots: dict[str, Path] = {}
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context(viewport=VIEWPORT, locale="ja-JP")
        page = context.new_page()
        ensure_logged_in(page)
        dismiss_overlays(page)
        # 新規ユーザーのコレクション・デッキ準備は ensure_playable_deck 内で行う

        mapping = {
            "register": (f"{BASE_URL}/register", True),
            "home": (f"{BASE_URL}/home", True),
            "pack": (f"{BASE_URL}/pack", True),
            "decks": (f"{BASE_URL}/decks", True),
            "how_to_play": (f"{BASE_URL}/how-to-play", True),
            "cpu_menu": (f"{BASE_URL}/battle/cpu/casual", True),
            "cpu_hub": (f"{BASE_URL}/battle/cpu", True),
            "friends": (f"{BASE_URL}/friends", True),
        }
        for key, (url, full_page) in mapping.items():
            pth = tmp / f"{key}.png"
            shot(page, pth, url=url, full_page=full_page)
            shots[key] = pth

        karyudo, nebula = capture_manual_card_screenshots(page, tmp)
        shots["card_karyudo"] = karyudo
        shots["card_field_nebula"] = nebula

        page.goto(f"{BASE_URL}/decks", wait_until="networkidle")
        dismiss_overlays(page)
        edit_link = page.locator('a[href*="/decks/"][href*="/edit"]').first
        deck_edit = tmp / "deck_edit.png"
        if edit_link.count():
            edit_link.click()
            page.wait_for_timeout(1200)
            dismiss_overlays(page)
            page.screenshot(path=str(deck_edit), full_page=True)
        else:
            page.screenshot(path=str(deck_edit), full_page=True)
        shots["deck_edit"] = deck_edit

        shots.update(simulate_battle_screenshots(page, tmp))
        shots["battle_league"] = simulate_league_battle_screenshot(page, tmp)

        browser.close()
    return shots


def main() -> int:
    if not re.match(r"^https?://", BASE_URL):
        print("Invalid BASE_URL", file=sys.stderr)
        return 1
    font = register_fonts()
    with tempfile.TemporaryDirectory(prefix="nu_manual_") as td:
        shots = capture_screenshots(Path(td))
        build_pdf(shots, font)
    print(f"Wrote: {OUT_PDF}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
