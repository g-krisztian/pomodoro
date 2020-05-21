(ns pomodoro.cookie-storage
  (:require [reagent.cookies :as rc]
            [reagent.core :as r]))

(defonce cache (r/atom {}))

(defn update-cache [k v]
  (swap! cache merge {k v}))

(defn get-n-cache [k]
  (let [v (rc/get k)]
    (update-cache k v)
    v))

(defn get-by-key [k]
  (or (k cache) (get-n-cache k)))

(defn set-by-key [k v]
  (rc/set! k v)
  (update-cache k v))

(defn delete [k]
  (swap! cache dissoc k)
  (rc/remove! k))

(defn get-next-key []
  (get-by-key :next-key))

(defn set-next-key [k]
  (set-by-key :next-key k))

(defn get-unit []
  (get-by-key :unit))

(defn set-unit [u]
  (set-by-key :unit u))

(defn get-plan []
  (get-by-key :plan))

(defn set-plan [p]
  (set-by-key :plan p))

(defn contains-plan? []
  (not-empty (get-by-key :plan)))

(defn delete-plan []
  (delete :plan))

(defn get-history []
  (get-by-key :history))

(defn set-history [h]
  (set-by-key :history h))

(defn delete-history []
  (delete :history))

(defn contains-history? []
  (not-empty (get-by-key :history)))