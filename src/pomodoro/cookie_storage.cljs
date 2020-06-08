(ns pomodoro.cookie-storage
  (:require [reagent.cookies :as rc]
            [reagent.core :as r]))

(defonce cache (r/atom {}))
(defonce source (r/atom :pomodoro))

(defn update-cache [k v]
  (swap! cache assoc k v))

(defn get-n-cache [k]
  (let [v (get (rc/get @source) k)]
    (update-cache k v)
    v))

(defn get-by-key [k]
  (or (get cache k) (get-n-cache k)))

(defn set-by-key [k v]
  (let [cache (update-cache k v)]
    (rc/set! @source cache)))

(defn delete [k]
  (->>
    (swap! cache dissoc k)
    (rc/set! @source)))

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

(defn get-key []
  (let [actual (or (get-next-key) 0)]
    (set-next-key (inc actual))
    actual))

(defn init [s]
  (reset! source s)
  (reset! cache (rc/get @source))
  (when-not (and (get-plan) (get-history))
    (set-next-key 0)))
