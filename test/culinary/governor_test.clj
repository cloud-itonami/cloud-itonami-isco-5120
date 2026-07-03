(ns culinary.governor-test
  (:require [clojure.test :refer [deftest is testing]]
            [culinary.store :as store]
            [culinary.governor :as governor]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-ticket! st {:ticket-id "ticket-1" :name "Table 7 Dinner Order"})
    st))

(deftest ok-on-clean-cook-support
  (let [st (fresh-store)
        proposal {:op :cook-support :effect :propose :confidence 0.9 :stake :low}
        v (governor/check {:ticket-id "ticket-1"} {} proposal st)]
    (is (:ok? v))
    (is (not (:hard? v)))
    (is (not (:escalate? v)))))

(deftest hard-on-unregistered-ticket
  (let [st (fresh-store)
        proposal {:op :cook-support :effect :propose :confidence 0.9 :stake :low}
        v (governor/check {:ticket-id "no-such-ticket"} {} proposal st)]
    (is (:hard? v))
    (is (some #(= :no-ticket (:rule %)) (:violations v)))))

(deftest hard-on-no-actuation-violation
  (let [st (fresh-store)
        proposal {:op :cook-support :effect :direct-write :confidence 0.9 :stake :low}
        v (governor/check {:ticket-id "ticket-1"} {} proposal st)]
    (is (:hard? v))
    (is (some #(= :no-actuation (:rule %)) (:violations v)))))

(deftest escalates-on-open-flame-operation
  (let [st (fresh-store)
        proposal {:op :operate-near-open-flame :effect :propose :confidence 0.9 :stake :high}
        v (governor/check {:ticket-id "ticket-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest escalates-on-allergen-cross-contact-handling
  (let [st (fresh-store)
        proposal {:op :handle-allergen-cross-contact :effect :propose :confidence 0.9 :stake :high}
        v (governor/check {:ticket-id "ticket-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest escalates-on-low-confidence
  (let [st (fresh-store)
        proposal {:op :cook-support :effect :propose :confidence 0.2 :stake :low}
        v (governor/check {:ticket-id "ticket-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest store-records-and-ledger-append-only
  (let [st (fresh-store)]
    (store/commit-record! st {:ticket-id "ticket-1" :op :plate})
    (store/append-ledger! st {:disposition :commit})
    (is (= 1 (count (store/records-of st "ticket-1"))))
    (is (= 1 (count (store/ledger st))))))
