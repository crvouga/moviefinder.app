(ns core.browser
  (:require
   [clojure.core.async :as a]))

(defn history-event-chan
  "Returns a channel that emits the history event when it occurs."
  []
  (let [chan (a/chan)]
    (doseq [event-name ["popstate" "pushstate" "replacestate"]]
      (println "adding event listener" event-name)
      (js/window.addEventListener event-name #(a/put! chan %)))
    chan))

(defn- ensure-slash [url]
  (if (.startsWith url "/")
    url
    (str "/" url)))

(defn push-state! [url]
  (js/window.history.pushState nil nil (ensure-slash url)))