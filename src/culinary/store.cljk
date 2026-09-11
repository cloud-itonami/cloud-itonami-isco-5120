(ns culinary.store
  "SSoT for the ISCO-08 5120 independent culinary sole-proprietor
  actor. Store is a protocol injected into the `culinary.actor`
  StateGraph — `MemStore` is the default, deterministic, zero-dep
  backend; a Datomic/kotoba-server-backed implementation can be
  swapped in without touching the actor or governor (itonami actor
  pattern, per ADR-2607011000 / CLAUDE.md Actors section).

  Domain:

    ticket   — a registered order ticket (:ticket-id, :name)
    record   — a committed operating record under a ticket
               (cook-support step, plating step, open-flame
               operation, allergen cross-contact handling) — written
               ONLY via commit-record!, never mutated in place
    ledger   — an append-only audit trail of every proposal/verdict/
               disposition, regardless of outcome (commit or hold)")

(defprotocol Store
  (ticket [s ticket-id])
  (records-of [s ticket-id])
  (ledger [s])
  (register-ticket! [s ticket])
  (commit-record! [s record])
  (append-ledger! [s fact]))

(defrecord MemStore [a]
  Store
  (ticket [_ ticket-id] (get-in @a [:tickets ticket-id]))
  (records-of [_ ticket-id] (filter #(= ticket-id (:ticket-id %)) (:records @a)))
  (ledger [_] (:ledger @a))
  (register-ticket! [s ticket]
    (swap! a assoc-in [:tickets (:ticket-id ticket)] ticket) s)
  (commit-record! [s record]
    (swap! a update :records (fnil conj []) record) s)
  (append-ledger! [s fact]
    (swap! a update :ledger (fnil conj []) fact) s))

(defn mem-store
  ([] (mem-store {}))
  ([seed] (->MemStore (atom (merge {:tickets {} :records [] :ledger []} seed)))))
