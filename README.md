# pomodoro

Pomodoro app in clojureScript

## Overview

You are able to 
 - run a single timer
 - plan a sequence of timers
 - run a plan
 - review the history
 - get a summary, grouped by tasks
 
## Technologies
|tool|goal|
|---|---|
| clojureScript | development     |
| leiningen     | build           |
| fighweel-main | live reload     |
| reagent       | react           |
| reagent-utils | cookie storage  |
| devcards      | testing         |
| cljs-bach     | make some noise |
 
## Setup

To get an interactive development environment run:

    lein fig:build

and open your browser at [localhost:9500](http://localhost:9500/).

For devcards interface open [localhost:9500/cards.html](http://localhost:9500/cards.html),
for tests [http://localhost:9500/figwheel-extra-main/auto-testing](http://localhost:9500/figwheel-extra-main/auto-testing).


This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein do clean, fig:min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL. 

## License

Copyright © 2020

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
