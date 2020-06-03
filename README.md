# pomodoro

Another pomodoro app

## Overview

You are able to 
 - run a single timer
 - plan a sequence of timers
 - run a plan
 - review the history
 - get a summary, grouped by tasks

## Setup

To get an interactive development environment run:

    lein fig:build

and open your browser at [localhost:9500](http://localhost:9500/).
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
