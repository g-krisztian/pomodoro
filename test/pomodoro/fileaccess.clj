(ns pomodoro.fileaccess
  (:require [clojure.java.io :as io]
            [clojure.walk :refer [keywordize-keys]]
            [clojure.tools.reader :as reader]))

(defmacro inline-resource [resource-path]
  (slurp (io/resource resource-path)))

(def dictionary-matcher #".*dictionary_([a-z]{2}).edn")

(defn dictionary-file? [file]
  (re-matches dictionary-matcher (.getName file)))

(defmacro files-in-dir [directory]
  (mapv (fn [x] (.getPath x)) (.listFiles (io/file directory))))

(defn extract-code [filename]
  (second (re-matches dictionary-matcher filename)))

(defn reduce-reader [m f]
  (assoc m (extract-code (.getName f)) (reader/read-string (slurp f))))

(defmacro read-all-dictionary [directory]
  (->> directory
       (io/file)
       (.listFiles)
       (remove #(.isDirectory %))
       (remove #(.isHidden %))
       (filter dictionary-file?)
       (reduce reduce-reader {})
       (keywordize-keys)))

(def all-dictionary (read-all-dictionary "resources/public"))
