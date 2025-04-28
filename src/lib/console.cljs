(ns lib.console
  (:require
   [cljs.reader :as cr]))

(defmulti cmd first)

(defn- -cmd [cmd-str]
  (try
    (let [cmd-data (cr/read-string cmd-str)]
      (cmd cmd-data))
    (catch js/Error e
      (js/console.error "Error executing command:" e))))

(set! (.-cmd js/window) -cmd)