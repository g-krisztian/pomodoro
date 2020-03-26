(ns pomodoro.audio
  (:require [cljs-bach.synthesis :as bach]))

(defonce sound-uri "110661_739427-hq.mp3")
(defonce audio-context (bach/audio-context))

(defonce boing 
  (bach/connect->
    (bach/sample sound-uri)
    bach/destination))
  
(defn playback-mp3 []
  (bach/run-with boing audio-context (bach/current-time audio-context) 3.0))

