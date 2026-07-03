(ns culinary.governor
  "CulinaryGovernor — the independent safety/traceability layer for
  the ISCO-08 5120 independent culinary actor. Wired as its own
  `:govern` node in `culinary.actor`'s StateGraph, downstream of
  `:advise` — the Advisor has no notion of ticket provenance or
  open-flame/allergen risk, so this MUST be a separate system able to
  reject a proposal (itonami actor pattern, per ADR-2607011000 /
  CLAUDE.md Actors section).

  `check` is a pure function of (request, context, proposal, store) ->
  verdict; it never mutates the store. The StateGraph's `:decide` node
  routes on the verdict:
    :hard? true                → :hold  (irreversible, no write)
    :escalate? true            → :request-approval (interrupt-before)
    otherwise                  → :commit

  HARD invariants (:hard? true, ALWAYS :hold, never overridable):
    1. ticket provenance   — the request's ticket must be registered.
    2. no-actuation        — proposal :effect must be :propose.
  ESCALATION invariants (:escalate? true, ALWAYS human sign-off, per the
  README robotics-premise: operating near open flame or hot surfaces, or
  allergen cross-contact handling, always requires human sign-off):
    3. :op :operate-near-open-flame.
    4. :op :handle-allergen-cross-contact.
    5. low confidence (< `confidence-floor`)."
  (:require [culinary.store :as store]))

(def confidence-floor 0.6)
(def ^:private escalating-ops #{:operate-near-open-flame :handle-allergen-cross-contact})

(defn- hard-violations [{:keys [proposal]} ticket-record]
  (cond-> []
    (nil? ticket-record)
    (conj {:rule :no-ticket :detail "未登録 ticket"})

    (not= :propose (:effect proposal))
    (conj {:rule :no-actuation :detail "effect は :propose のみ許可（直接書込禁止）"})))

(defn check
  "Assess a proposal against `request`/`context`/`proposal` and a
  `store` implementing `culinary.store/Store`. Returns
  `{:ok? bool :violations [...] :confidence n :hard? bool :escalate? bool}`."
  [request context proposal store]
  (let [ticket-record (store/ticket store (:ticket-id request))
        hard (hard-violations {:proposal proposal} ticket-record)
        hard? (boolean (seq hard))
        conf (or (:confidence proposal) 0.0)
        low? (< conf confidence-floor)
        risky-op? (contains? escalating-ops (:op proposal))]
    {:ok? (and (not hard?) (not low?) (not risky-op?))
     :violations hard
     :confidence conf
     :hard? hard?
     :escalate? (and (not hard?) (or low? risky-op?))}))
