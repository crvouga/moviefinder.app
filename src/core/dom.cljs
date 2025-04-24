(ns core.dom
  (:require
   [clojure.core.async :as a]))

(defn query! [css-selector]
  (js/document.querySelector css-selector))

(defn event-chan
  "Creates a channel that listens for DOM events on a given DOM node.
   Returns a channel that will receive events when they occur."
  [^js dom-node event-name]
  (let [event-chan (a/chan)]
    (.addEventListener dom-node event-name (fn [event] (a/put! event-chan event)))
    event-chan))
