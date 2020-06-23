(ns pomodoro.dictionary)

(defn get
  ([state key]
   (get-in @state [:dictionary :long key]))
  ([state key width]
   (let [short-value (get-in @state [:dictionary :short key])
         long-value (get-in @state [:dictionary :long key])]
     (or short-value long-value))))