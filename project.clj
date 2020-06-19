(defproject pomodoro "0.1.0-SNAPSHOT"
  :description "Simple pomodoro app"
  :url "https://github.com/g-krisztian/pomodoro"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.9.1"

  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.520"]
                 [org.clojure/core.async  "0.4.500"]
                 [cljs-ajax "0.8.0"]
                 [reagent "0.10.0"]
                 [reagent-utils "0.3.3"]
                 [devcards "0.2.7"]
                 [cljs-bach "0.3.0"]]

  :source-paths ["src" "test"]
  :resource-paths ["resources" "target"]
  :clean-targets ^{:protect false} [:target-path "target" "resources/public/cljs-out"]

  :aliases {"fig"       ["trampoline" "run" "-m" "figwheel.main"]
            "fig:build" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]
            "fig:min"   ["run" "-m" "figwheel.main" "-O" "advanced" "-bo" "dev"]}

  :profiles {:dev {:dependencies [[com.bhauman/figwheel-main "0.2.3"]
                                  [com.bhauman/rebel-readline-cljs "0.1.4"]
                                  [devcards "0.2.7"]]}})
