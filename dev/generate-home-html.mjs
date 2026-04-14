/**
 * home.html を UTF-8 で生成（ソースは ASCII のみ。日本語は \\u エスケープ）
 */
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const out = path.join(__dirname, "..", "src", "main", "resources", "templates", "home.html");

const u = (s) => s; // 可読用（実体は下のテンプレート内 \u）

const html = `<!DOCTYPE html>
<html lang="ja" xmlns:th="http://www.thymeleaf.org">
<head>
	<meta charset="UTF-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1"/>
	<title>${u("Nine Universe \u2014 \u30db\u30fc\u30e0")}</title>
	<link rel="stylesheet" th:href="@{/css/app.css(v=\${assetVersion})}"/>
</head>
<body class="page page--theme-arcane"
      th:attr="data-announce-epoch=\${announcementUiEpoch},
               data-perf-claimable=\${perfLightAnnouncementClaimable and announcementListPerfLight},
               data-ttp-claimable=\${timePackAnnouncementClaimable and announcementListTimePack},
               data-balance-claimable=\${balanceUiMissionAnnouncementClaimable and announcementListBalanceUiMission},
               data-pack-rates-claimable=\${packRatesAnnouncementClaimable and announcementListPackRates},
               data-pack-result-draw-again-claimable=\${packResultDrawAgainAnnouncementClaimable and announcementListPackResultDrawAgain}">
<header class="topbar">
	<div th:replace="~{fragments/game-title-brand :: brand}"></div>
	<div class="topbar__meta">
		<button type="button"
		        class="btn btn--ghost topbar__announce"
		        th:classappend="\${(perfLightAnnouncementClaimable and announcementListPerfLight) or (timePackAnnouncementClaimable and announcementListTimePack) or (balanceUiMissionAnnouncementClaimable and announcementListBalanceUiMission) or (packRatesAnnouncementClaimable and announcementListPackRates) or (packResultDrawAgainAnnouncementClaimable and announcementListPackResultDrawAgain) or (timePackAvailablePacks != null and timePackAvailablePacks > 0)} ? ' topbar__announce--highlight' : ''"
		        id="announcement-open"
		        aria-haspopup="dialog"
		        aria-controls="announcement-modal">
			<span class="topbar__announce-label">
				<span class="topbar__announce-dot" id="announcement-unread-dot" hidden aria-hidden="true"></span>
				\u304a\u77e5\u3089\u305b
			</span>
		</button>
		<span>\u30b8\u30a7\u30e0: <strong th:text="\${user.coins}">0</strong></span>
		<form th:action="@{/logout}" method="post" class="inline">
			<input type="hidden" th:name="\${_csrf.parameterName}" th:value="\${_csrf.token}"/>
			<button type="submit" class="btn btn--ghost">\u30b2\u30fc\u30e0\u3092\u3084\u3081\u308b\uff08\u30ed\u30b0\u30a2\u30a6\u30c8\uff09</button>
		</form>
	</div>
</header>

<div id="announcement-modal"
     class="announcement-modal"
     hidden
     role="dialog"
     aria-modal="true"
     aria-labelledby="announcement-modal-title"
     aria-hidden="true">
	<div class="announcement-modal__panel" role="document">
		<h2 id="announcement-modal-title" class="announcement-modal__heading">\u304a\u77e5\u3089\u305b</h2>

		<article class="announcement-modal__card"
		         th:if="\${announcementListPerfLight and (perfLightAnnouncementClaimable or perfLightAnnouncementClaimed or perfLightAnnouncementExpiredUnclaimed or perfLightAnnouncementFutureUnclaimed)}">
			<div class="announcement-modal__mailbox">
				<div class="announcement-modal__mailbox-body">
					<p class="announcement-modal__card-text">\u300c\u304a\u77e5\u3089\u305b\u300d\u306e\u8ffd\u52a0\u3001\u51e6\u7406\u52d5\u4f5c\u306e\u8efd\u91cf\u5316\u3092\u3057\u307e\u3057\u305f\u3002</p>
				</div>
				<aside class="announcement-modal__mailbox-aside">
					<span th:if="\${perfLightAnnouncementClaimable}" class="announcement-modal__deadline">\u53d7\u3051\u53d6\u308a\u671f\u9650 30\u65e5</span>
					<form th:if="\${perfLightAnnouncementClaimable}"
					      th:action="@{/home/announcements/perf-light/claim}"
					      method="post"
					      class="announcement-modal__gem-form">
						<input type="hidden" th:name="\${_csrf.parameterName}" th:value="\${_csrf.token}"/>
						<button type="submit" class="btn btn--primary announcement-modal__gem-btn"
						        th:text="|\${perfLightAnnouncementGemAmount}\u30b8\u30a7\u30e0|">10\u30b8\u30a7\u30e0</button>
					</form>
					<p th:if="\${perfLightAnnouncementClaimed}" class="announcement-modal__card-status ok">\u53d7\u3051\u53d6\u308a\u6e08\u307f</p>
					<p th:if="\${perfLightAnnouncementExpiredUnclaimed}" class="announcement-modal__card-status muted">\u53d7\u3051\u53d6\u308a\u671f\u9650\u304c\u904e\u304e\u307e\u3057\u305f</p>
					<p th:if="\${perfLightAnnouncementFutureUnclaimed}" class="announcement-modal__card-status muted">\u53d7\u3051\u53d6\u308a\u958b\u59cb\u524d\u3067\u3059</p>
				</aside>
			</div>
		</article>

		<article class="announcement-modal__card"
		         th:if="\${announcementListTimePack and (timePackAnnouncementClaimable or timePackAnnouncementClaimed or timePackAnnouncementExpiredUnclaimed or timePackAnnouncementFutureUnclaimed)}">
			<div class="announcement-modal__mailbox">
				<div class="announcement-modal__mailbox-body">
					<p class="announcement-modal__card-text">\u6642\u9593\u3067\u30d1\u30c3\u30af\u958b\u5c01\u3092\u5b9f\u88c5\u3057\u307e\u3057\u305f\u3002</p>
				</div>
				<aside class="announcement-modal__mailbox-aside">
					<span th:if="\${timePackAnnouncementClaimable}" class="announcement-modal__deadline">\u53d7\u3051\u53d6\u308a\u671f\u9650 30\u65e5</span>
					<form th:if="\${timePackAnnouncementClaimable}"
					      th:action="@{/home/announcements/time-pack/claim}"
					      method="post"
					      class="announcement-modal__gem-form">
						<input type="hidden" th:name="\${_csrf.parameterName}" th:value="\${_csrf.token}"/>
						<button type="submit" class="btn btn--primary announcement-modal__gem-btn"
						        th:text="|\${timePackAnnouncementGemAmount}\u30b8\u30a7\u30e0|">10\u30b8\u30a7\u30e0</button>
					</form>
					<p th:if="\${timePackAnnouncementClaimed}" class="announcement-modal__card-status ok">\u53d7\u3051\u53d6\u308a\u6e08\u307f</p>
					<p th:if="\${timePackAnnouncementExpiredUnclaimed}" class="announcement-modal__card-status muted">\u53d7\u3051\u53d6\u308a\u671f\u9650\u304c\u904e\u304e\u307e\u3057\u305f</p>
					<p th:if="\${timePackAnnouncementFutureUnclaimed}" class="announcement-modal__card-status muted">\u53d7\u3051\u53d6\u308a\u958b\u59cb\u524d\u3067\u3059</p>
				</aside>
			</div>
		</article>

		<article class="announcement-modal__card"
		         th:if="\${announcementListBalanceUiMission and (balanceUiMissionAnnouncementClaimable or balanceUiMissionAnnouncementClaimed or balanceUiMissionAnnouncementExpiredUnclaimed or balanceUiMissionAnnouncementFutureUnclaimed)}">
			<div class="announcement-modal__mailbox">
				<div class="announcement-modal__mailbox-body">
					<p class="announcement-modal__card-text">\u30fbUI\u30fb\u30df\u30c3\u30b7\u30e7\u30f3\u306e\u898b\u76f4\u3057\u3092\u3057\u307e\u3057\u305f\u3002</p>
					<p class="announcement-modal__card-text">\u30fb\u300c\u79d1\u5b66\u8005\u300d\u300c\u30d4\u30af\u30b7\u30fc\u300d\u300c\u98a8\u306e\u9b54\u4eba\u300d\u300c\u30c0\u30fc\u30af\u30c9\u30e9\u30b4\u30f3\u300d\u306e\u30d0\u30e9\u30f3\u30b9\u3092\u8abf\u6574\u3057\u307e\u3057\u305f\u3002</p>
				</div>
				<aside class="announcement-modal__mailbox-aside">
					<span th:if="\${balanceUiMissionAnnouncementClaimable}" class="announcement-modal__deadline">\u53d7\u3051\u53d6\u308a\u671f\u9650 30\u65e5</span>
					<form th:if="\${balanceUiMissionAnnouncementClaimable}"
					      th:action="@{/home/announcements/balance-ui-mission/claim}"
					      method="post"
					      class="announcement-modal__gem-form">
						<input type="hidden" th:name="\${_csrf.parameterName}" th:value="\${_csrf.token}"/>
						<button type="submit" class="btn btn--primary announcement-modal__gem-btn"
						        th:text="|\${balanceUiMissionAnnouncementGemAmount}\u30b8\u30a7\u30e0|">10\u30b8\u30a7\u30e0</button>
					</form>
					<p th:if="\${balanceUiMissionAnnouncementClaimed}" class="announcement-modal__card-status ok">\u53d7\u3051\u53d6\u308a\u6e08\u307f</p>
					<p th:if="\${balanceUiMissionAnnouncementExpiredUnclaimed}" class="announcement-modal__card-status muted">\u53d7\u3051\u53d6\u308a\u671f\u9650\u304c\u904e\u304e\u307e\u3057\u305f</p>
					<p th:if="\${balanceUiMissionAnnouncementFutureUnclaimed}" class="announcement-modal__card-status muted">\u53d7\u3051\u53d6\u308a\u958b\u59cb\u524d\u3067\u3059</p>
				</aside>
			</div>
		</article>

		<article class="announcement-modal__card"
		         th:if="\${announcementListPackRates and (packRatesAnnouncementClaimable or packRatesAnnouncementClaimed or packRatesAnnouncementExpiredUnclaimed or packRatesAnnouncementFutureUnclaimed)}">
			<div class="announcement-modal__mailbox">
				<div class="announcement-modal__mailbox-body">
					<p class="announcement-modal__card-text">\u30fb\u30ab\u30fc\u30c9\u30d1\u30c3\u30af\u306e\u6392\u51fa\u7387\u3092\u8abf\u6574\u3057\u307e\u3057\u305f</p>
				</div>
				<aside class="announcement-modal__mailbox-aside">
					<span th:if="\${packRatesAnnouncementClaimable}" class="announcement-modal__deadline">\u53d7\u3051\u53d6\u308a\u671f\u9650 30\u65e5</span>
					<form th:if="\${packRatesAnnouncementClaimable}"
					      th:action="@{/home/announcements/pack-rates/claim}"
					      method="post"
					      class="announcement-modal__gem-form">
						<input type="hidden" th:name="\${_csrf.parameterName}" th:value="\${_csrf.token}"/>
						<button type="submit" class="btn btn--primary announcement-modal__gem-btn"
						        th:text="|\${packRatesAnnouncementGemAmount}\u30b8\u30a7\u30e0|">3\u30b8\u30a7\u30e0</button>
					</form>
					<p th:if="\${packRatesAnnouncementClaimed}" class="announcement-modal__card-status ok">\u53d7\u3051\u53d6\u308a\u6e08\u307f</p>
					<p th:if="\${packRatesAnnouncementExpiredUnclaimed}" class="announcement-modal__card-status muted">\u53d7\u3051\u53d6\u308a\u671f\u9650\u304c\u904e\u304e\u307e\u3057\u305f</p>
					<p th:if="\${packRatesAnnouncementFutureUnclaimed}" class="announcement-modal__card-status muted">\u53d7\u3051\u53d6\u308a\u958b\u59cb\u524d\u3067\u3059</p>
				</aside>
			</div>
		</article>

		<article class="announcement-modal__card"
		         th:if="\${announcementListPackResultDrawAgain and (packResultDrawAgainAnnouncementClaimable or packResultDrawAgainAnnouncementClaimed or packResultDrawAgainAnnouncementExpiredUnclaimed or packResultDrawAgainAnnouncementFutureUnclaimed)}">
			<div class="announcement-modal__mailbox">
				<div class="announcement-modal__mailbox-body">
					<p class="announcement-modal__card-text">\u30fb\u30ab\u30fc\u30c9\u30d1\u30c3\u30af\u3092\u5f15\u3044\u305f\u3042\u3068\u3001\u7d50\u679c\u753b\u9762\u3067\u300c\u3082\u3046\u4e00\u5ea6\u5f15\u304f\u300d\u30dc\u30bf\u30f3\u3092\u8ffd\u52a0\u3057\u307e\u3057\u305f</p>
				</div>
				<aside class="announcement-modal__mailbox-aside">
					<span th:if="\${packResultDrawAgainAnnouncementClaimable}" class="announcement-modal__deadline">\u53d7\u3051\u53d6\u308a\u671f\u9650 30\u65e5</span>
					<form th:if="\${packResultDrawAgainAnnouncementClaimable}"
					      th:action="@{/home/announcements/pack-result-draw-again/claim}"
					      method="post"
					      class="announcement-modal__gem-form">
						<input type="hidden" th:name="\${_csrf.parameterName}" th:value="\${_csrf.token}"/>
						<button type="submit" class="btn btn--primary announcement-modal__gem-btn"
						        th:text="|\${packResultDrawAgainAnnouncementGemAmount}\u30b8\u30a7\u30e0|">5\u30b8\u30a7\u30e0</button>
					</form>
					<p th:if="\${packResultDrawAgainAnnouncementClaimed}" class="announcement-modal__card-status ok">\u53d7\u3051\u53d6\u308a\u6e08\u307f</p>
					<p th:if="\${packResultDrawAgainAnnouncementExpiredUnclaimed}" class="announcement-modal__card-status muted">\u53d7\u3051\u53d6\u308a\u671f\u9650\u304c\u904e\u304e\u307e\u3057\u305f</p>
					<p th:if="\${packResultDrawAgainAnnouncementFutureUnclaimed}" class="announcement-modal__card-status muted">\u53d7\u3051\u53d6\u308a\u958b\u59cb\u524d\u3067\u3059</p>
				</aside>
			</div>
		</article>
		<p th:if="\${flashAnnouncementSuccess}" class="announcement-modal__flash ok" th:text="\${flashAnnouncementSuccess}">\u53d7\u3051\u53d6\u308a\u307e\u3057\u305f\u3002</p>
		<p th:if="\${flashAnnouncementError}" class="announcement-modal__flash err" th:text="\${flashAnnouncementError}">\u30a8\u30e9\u30fc</p>
		<button type="button" class="btn btn--ghost announcement-modal__close" id="announcement-close">\u9589\u3058\u308b</button>
	</div>
</div>

<div id="welcome-bonus-modal"
     class="welcome-bonus-modal"
     th:if="\${welcomeHomeBonusShown}"
     role="dialog"
     aria-modal="true"
     aria-labelledby="welcome-bonus-title">
	<div class="welcome-bonus-modal__panel">
		<h2 id="welcome-bonus-title" class="welcome-bonus-modal__heading">\u65b0\u898f\u30e6\u30fc\u30b6\u30fc\u30d7\u30ec\u30bc\u30f3\u30c8\uff01</h2>
		<p class="welcome-bonus-modal__text"
		   th:text="|\${welcomeHomeBonusAmount}\u30b8\u30a7\u30e0\u3092\u4ed8\u4e0e\u3057\u307e\u3057\u305f\u3002|">30\u30b8\u30a7\u30e0\u3092\u4ed8\u4e0e\u3057\u307e\u3057\u305f\u3002</p>
		<button type="button" class="btn btn--primary welcome-bonus-modal__close" id="welcome-bonus-close">\u9589\u3058\u308b</button>
	</div>
</div>

<div class="container">
	<section class="home-time-pack"
	         th:attr="data-duration-ms=\${timePackDurationMs},data-start-ms=\${timePackCycleStartEpochMs}">
		<div class="home-time-pack__head">
			<h2 class="home-time-pack__title">\u30dc\u30fc\u30ca\u30b9\u30d1\u30c3\u30af</h2>
			<div class="home-time-pack__lead">
				<p class="home-time-pack__lead-line">\u6642\u9593\u306e\u7d4c\u904e\u3067\u30b2\u30fc\u30b8\u304c\u6e9c\u307e\u308a\u307e\u3059\uff081\u5468<strong>12\u6642\u9593</strong>\uff09\u3002</p>
				<ul class="home-time-pack__bullets">
					<li><span class="home-time-pack__tier">50%</span> \u2026 \u30b9\u30bf\u30f3\u30c0\u30fc\u30c9\u30d1\u30c3\u30af\u3092 <strong>1\u30d1\u30c3\u30af</strong> \u958b\u5c01\u3067\u304d\u307e\u3059\u3002</li>
					<li><span class="home-time-pack__tier">MAX</span> \u2026 <strong>2\u30d1\u30c3\u30af</strong> \u307e\u3068\u3081\u3066\u958b\u5c01\u3067\u304d\u307e\u3059\uff08\u958b\u5c01\u3067\u30b2\u30fc\u30b8\u30ea\u30bb\u30c3\u30c8\uff09\u3002</li>
				</ul>
			</div>
		</div>
		<div class="home-time-pack__bar-wrap" aria-hidden="true">
			<div class="home-time-pack__ruler home-time-pack__ruler--labels">
				<div class="home-time-pack__ruler-point" style="left:50%">
					<span class="home-time-pack__scale-label">50%</span>
				</div>
				<div class="home-time-pack__ruler-point home-time-pack__ruler-point--max" style="left:100%">
					<span class="home-time-pack__scale-label">MAX</span>
				</div>
			</div>
			<div class="home-time-pack__track">
				<div class="home-time-pack__fill"
				     th:style="|width: \${timePackFillPercent}%;|"
				     id="time-pack-fill"></div>
			</div>
			<div class="home-time-pack__ruler home-time-pack__ruler--times">
				<div class="home-time-pack__ruler-point" style="left:50%">
					<span class="home-time-pack__cd" id="time-pack-cd-half">0\u6642\u95930\u52060\u79d2</span>
				</div>
				<div class="home-time-pack__ruler-point home-time-pack__ruler-point--max" style="left:100%">
					<span class="home-time-pack__cd" id="time-pack-cd-max">0\u6642\u95930\u52060\u79d2</span>
				</div>
			</div>
		</div>
		<p class="home-time-pack__status" id="time-pack-status">
			<span th:if="\${timePackAvailablePacks == 1}">\u30b9\u30bf\u30f3\u30c0\u30fc\u30c9\u30d1\u30c3\u30af\u3092 <strong>1\u30d1\u30c3\u30af</strong> \u958b\u5c01\u3067\u304d\u307e\u3059\u3002</span>
			<span th:if="\${timePackAvailablePacks == 2}">\u30b9\u30bf\u30f3\u30c0\u30fc\u30c9\u30d1\u30c3\u30af\u3092 <strong>2\u30d1\u30c3\u30af</strong> \u307e\u3068\u3081\u3066\u958b\u5c01\u3067\u304d\u307e\u3059\u3002</span>
		</p>
		<form th:action="@{/home/time-pack/open}" method="post" class="home-time-pack__form">
			<input type="hidden" th:name="\${_csrf.parameterName}" th:value="\${_csrf.token}"/>
			<button type="submit"
			        class="btn btn--primary home-time-pack__btn"
			        id="time-pack-open-btn"
			        th:disabled="\${timePackAvailablePacks == 0}"
			        th:text="\${timePackAvailablePacks == 2} ? '\u30b9\u30bf\u30f3\u30c0\u30fc\u30c9\u30922\u30d1\u30c3\u30af\u958b\u5c01' : (\${timePackAvailablePacks == 1} ? '\u30b9\u30bf\u30f3\u30c0\u30fc\u30c9\u30921\u30d1\u30c3\u30af\u958b\u5c01' : '\u958b\u5c01\u3067\u304d\u308b\u307e\u3067\u5f85\u6a5f\u4e2d')">
				\u958b\u5c01\u3067\u304d\u308b\u307e\u3067\u5f85\u6a5f\u4e2d
			</button>
		</form>
		<p th:if="\${flashTimePackError}" class="home-time-pack__err" th:text="\${flashTimePackError}">\u30a8\u30e9\u30fc</p>
	</section>

	<section class="home-hero" aria-label="\u30e1\u30a4\u30f3\u30e1\u30cb\u30e5\u30fc">
		<div class="home-hero__bg" aria-hidden="true"></div>
		<div class="home-hero__body">
			<div class="home-hero__stats">
				<div class="stat-pill" role="status" aria-label="\u6240\u6301\u30b8\u30a7\u30e0">
					<span class="stat-pill__label">\u30b8\u30a7\u30e0</span>
					<span class="stat-pill__value" th:text="\${user.coins}">0</span>
				</div>
				<a class="btn btn--primary home-hero__cta" th:href="@{/pack}">\u30d1\u30c3\u30af\u8cfc\u5165\u3078</a>
				<a class="btn btn--ghost home-hero__cta" th:href="@{/decks}">\u30c7\u30c3\u30ad\u7de8\u96c6</a>
			</div>
		</div>
	</section>

	<main class="home-grid" aria-label="\u30e1\u30cb\u30e5\u30fc">
		<a class="tile tile--featured" th:href="@{/pack}">
			<span class="tile__icon tile__icon--mono" aria-hidden="true"><svg class="tile__icon-svg" viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="5" width="12" height="16" rx="1"/><rect x="8" y="3" width="12" height="16" rx="1"/></svg></span>
			<span class="tile__label">\u30ab\u30fc\u30c9\u30d1\u30c3\u30af\u3092\u8cfc\u5165</span>
			<span class="tile__hint">\u65b0\u305f\u306a\u30d5\u30a1\u30a4\u30bf\u30fc\u3092\u4ef2\u9593\u306b</span>
		</a>
		<a class="tile" th:href="@{/battle/cpu}">
			<span class="tile__icon tile__icon--mono" aria-hidden="true"><svg class="tile__icon-svg" viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round"><path d="M7 5l10 14M17 5L7 19"/></svg></span>
			<span class="tile__label">\u3072\u3068\u308a\u3067\u5bfe\u6226</span>
			<span class="tile__hint">CPU\u3068\u30d0\u30c8\u30eb</span>
		</a>
		<a class="tile" th:href="@{/battle/pvp}">
			<span class="tile__icon tile__icon--mono" aria-hidden="true"><svg class="tile__icon-svg" viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3l8 5v8l-8 5-8-5V8l8-5z"/><line x1="12" y1="8" x2="12" y2="21"/></svg></span>
			<span class="tile__label">\u3060\u308c\u304b\u3068\u5bfe\u6226</span>
			<span class="tile__hint">\u5bfe\u4eba\u6226\u3067\u8155\u8a66\u3057</span>
		</a>
		<a class="tile" th:href="@{/decks}">
			<span class="tile__icon tile__icon--mono" aria-hidden="true"><svg class="tile__icon-svg" viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="5" width="8" height="5" rx="0.8"/><rect x="3" y="12" width="8" height="7" rx="0.8"/><rect x="13" y="5" width="8" height="14" rx="0.8"/></svg></span>
			<span class="tile__label">\u30c7\u30c3\u30ad\u3092\u4f5c\u6210</span>
			<span class="tile__hint">\u30d0\u30c8\u30eb\u306e\u6e96\u5099</span>
		</a>
		<a class="tile" th:href="@{/library}">
			<span class="tile__icon tile__icon--mono" aria-hidden="true"><svg class="tile__icon-svg" viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><rect x="5" y="3" width="14" height="18" rx="1.2"/><line x1="8" y1="8" x2="16" y2="8"/><line x1="8" y1="12" x2="16" y2="12"/><line x1="8" y1="16" x2="14" y2="16"/></svg></span>
			<span class="tile__label">\u30e9\u30a4\u30d6\u30e9\u30ea</span>
			<span class="tile__hint">\u3059\u3079\u3066\u306e\u30ab\u30fc\u30c9\u3092\u78ba\u8a8d</span>
		</a>
		<a class="tile tile--mission-tile" th:href="@{/missions}">
			<span class="tile__icon tile__icon--mono" aria-hidden="true"><svg class="tile__icon-svg" viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.75"><circle cx="12" cy="12" r="9"/><circle cx="12" cy="12" r="5"/><circle cx="12" cy="12" r="1.5" fill="currentColor" stroke="none"/></svg></span>
			<span class="tile__label tile__label--notify">
				<span class="tile__label__text">\u30df\u30c3\u30b7\u30e7\u30f3</span>
				<span class="tile__mission-dot" th:if="\${missionHasUnclaimedReward}" aria-hidden="true"></span>
			</span>
			<span class="tile__hint">\u30b8\u30a7\u30e0\u3092\u7372\u5f97</span>
		</a>
		<a class="tile" th:href="@{/how-to-play}">
			<span class="tile__icon tile__icon--mono" aria-hidden="true"><svg class="tile__icon-svg" viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><path d="M6 4h7a3 3 0 013 3v13H9a3 3 0 01-3-3V4z"/><path d="M9 4v13h7"/></svg></span>
			<span class="tile__label">\u904a\u3073\u65b9</span>
			<span class="tile__hint">\u30eb\u30fc\u30eb\u3092\u78ba\u8a8d</span>
		</a>
	</main>

</div>
<script th:if="\${welcomeHomeBonusShown}">
(function () {
	var modal = document.getElementById('welcome-bonus-modal');
	var btn = document.getElementById('welcome-bonus-close');
	if (!modal || !btn) return;
	document.body.style.overflow = 'hidden';
	btn.focus();
	btn.addEventListener('click', function () {
		modal.hidden = true;
		document.body.style.overflow = '';
	});
})();
</script>
<script>
(function () {
	var ann = document.getElementById('announcement-modal');
	var openBtn = document.getElementById('announcement-open');
	var closeBtn = document.getElementById('announcement-close');
	if (!ann || !openBtn || !closeBtn) return;
	function openAnn() {
		ann.hidden = false;
		ann.setAttribute('aria-hidden', 'false');
		document.body.style.overflow = 'hidden';
		closeBtn.focus();
	}
	function closeAnn() {
		ann.hidden = true;
		ann.setAttribute('aria-hidden', 'true');
		document.body.style.overflow = '';
		openBtn.focus();
		var epoch = document.body.getAttribute('data-announce-epoch');
		if (epoch) {
			try {
				localStorage.setItem('nu_ann_ack', epoch);
			} catch (e) { /* ignore */ }
		}
		if (typeof window.syncAnnouncementUnreadDot === 'function') {
			window.syncAnnouncementUnreadDot();
		}
	}
	openBtn.addEventListener('click', openAnn);
	closeBtn.addEventListener('click', closeAnn);
	ann.addEventListener('click', function (e) {
		if (e.target === ann) closeAnn();
	});
	document.addEventListener('keydown', function (e) {
		if (e.key === 'Escape' && !ann.hidden) closeAnn();
	});
	/* \u53d7\u3051\u53d6\u308a\u76f4\u5f8c\u306e\u30d5\u30e9\u30c3\u30b7\u30e5\u30e1\u30c3\u30bb\u30fc\u30b8\u304c\u3042\u308b\u3068\u304d\u306f\u81ea\u52d5\u3067\u958b\u304f */
	if (document.querySelector('.announcement-modal__flash')) {
		openAnn();
	}
})();
</script>
<script>
(function () {
	var dot = document.getElementById('announcement-unread-dot');
	function syncAnnouncementUnreadDot() {
		if (!dot) return;
		var epoch = document.body.getAttribute('data-announce-epoch') || '';
		var perf = document.body.getAttribute('data-perf-claimable') === 'true';
		var ttp = document.body.getAttribute('data-ttp-claimable') === 'true';
		var bal = document.body.getAttribute('data-balance-claimable') === 'true';
		var pack = document.body.getAttribute('data-pack-rates-claimable') === 'true';
		var drawAgain = document.body.getAttribute('data-pack-result-draw-again-claimable') === 'true';
		var ack = '';
		try {
			ack = localStorage.getItem('nu_ann_ack') || '';
		} catch (e) {
			ack = '';
		}
		var unseen = epoch !== '' && ack !== epoch;
		var show = perf || ttp || bal || pack || drawAgain || unseen;
		dot.hidden = !show;
		dot.setAttribute('aria-hidden', show ? 'false' : 'true');
	}
	window.syncAnnouncementUnreadDot = syncAnnouncementUnreadDot;
	syncAnnouncementUnreadDot();
})();
</script>
<script>
(function () {
	var root = document.querySelector('.home-time-pack');
	var fill = document.getElementById('time-pack-fill');
	var btn = document.getElementById('time-pack-open-btn');
	var statusEl = document.getElementById('time-pack-status');
	var cdHalf = document.getElementById('time-pack-cd-half');
	var cdMax = document.getElementById('time-pack-cd-max');
	if (!root || !fill || !btn || !statusEl || !cdHalf || !cdMax) return;
	var dur = parseInt(root.getAttribute('data-duration-ms'), 10);
	var start = parseInt(root.getAttribute('data-start-ms'), 10);
	if (!dur || isNaN(start)) return;
	function fmtHms(ms) {
		if (ms <= 0) return '0\u6642\u95930\u52060\u79d2';
		var s = Math.floor(ms / 1000);
		var h = Math.floor(s / 3600);
		var m = Math.floor((s % 3600) / 60);
		var sec = s % 60;
		return h + '\u6642\u9593' + m + '\u5206' + sec + '\u79d2';
	}
	function tick() {
		var elapsed = Date.now() - start;
		if (elapsed < 0) elapsed = 0;
		var ratio = Math.min(1, elapsed / dur);
		var pct = Math.round(ratio * 100);
		fill.style.width = pct + '%';
		var packs = ratio >= 1 ? 2 : (ratio >= 0.5 ? 1 : 0);
		btn.disabled = packs === 0;
		var halfAt = start + Math.floor(dur / 2);
		var maxAt = start + dur;
		var toHalf = halfAt - Date.now();
		var toMax = maxAt - Date.now();
		if (ratio < 0.5) {
			cdHalf.textContent = fmtHms(toHalf);
		} else {
			cdHalf.textContent = '0\u6642\u95930\u52060\u79d2';
		}
		if (ratio < 1) {
			cdMax.textContent = fmtHms(toMax);
		} else {
			cdMax.textContent = '0\u6642\u95930\u52060\u79d2';
		}
		if (packs === 2) {
			btn.textContent = '\u30b9\u30bf\u30f3\u30c0\u30fc\u30c9\u30922\u30d1\u30c3\u30af\u958b\u5c01';
			statusEl.innerHTML = '\u30b9\u30bf\u30f3\u30c0\u30fc\u30c9\u30d1\u30c3\u30af\u3092 <strong>2\u30d1\u30c3\u30af</strong> \u307e\u3068\u3081\u3066\u958b\u5c01\u3067\u304d\u307e\u3059\u3002';
		} else if (packs === 1) {
			btn.textContent = '\u30b9\u30bf\u30f3\u30c0\u30fc\u30c9\u30921\u30d1\u30c3\u30af\u958b\u5c01';
			statusEl.innerHTML = '\u30b9\u30bf\u30f3\u30c0\u30fc\u30c9\u30d1\u30c3\u30af\u3092 <strong>1\u30d1\u30c3\u30af</strong> \u958b\u5c01\u3067\u304d\u307e\u3059\u3002';
		} else {
			btn.textContent = '\u958b\u5c01\u3067\u304d\u308b\u307e\u3067\u5f85\u6a5f\u4e2d';
			statusEl.textContent = '';
		}
	}
	tick();
	setInterval(tick, 1000);
})();
</script>
</body>
</html>
`;

fs.writeFileSync(out, html, "utf8");
console.log("Wrote", out);
