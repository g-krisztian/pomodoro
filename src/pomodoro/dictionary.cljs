(ns pomodoro.dictionary
  (:require [pomodoro.action :as action]))

(defn span
  ([state visibility]
   [:span {:class "input-group-text"
           :id    "text-meter"
           :ref   #(when-not (:ready @state) (action/set-ready state true))
           :style {
                   :visibility   visibility
                   :margin       "auto"
                   :width        "max-content"
                   :paddingLeft  "0.5rem"
                   :paddingRight "0.5rem"}}])
  ([state visibility t]
   (into (span state visibility) t)))

(defn set-text [text]
  (set! (.. js/document (getElementById "text-meter") -textContent) text))

(defn get-text-in-pixels []
  (.. js/document (getElementById "text-meter") -offsetWidth))

(def measure-text (memoize (fn [text]
                             (set-text text)
                             (get-text-in-pixels))))

(defn get-text
  ([state key]
   (get-in @state [:dictionary :long key]))
  ([state key width]
   (let [short-value (get-in @state [:dictionary :short key])
         long-value (get-in @state [:dictionary :long key])]
     (try
       (if (<= (measure-text long-value) width)
         long-value
         (or short-value long-value))
       (catch js/Object e long-value)))))
