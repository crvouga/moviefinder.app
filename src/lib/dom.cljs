(ns lib.dom
  (:require
   [clojure.core.async :as a]))

(defn query-selector [css-selector]
  (js/document.querySelector css-selector))

(defn add-event-listener [dom-node event-name callback]
  (when dom-node
    (.addEventListener dom-node event-name callback)))

(defn put-events! [event-chan css-selector event-name]
  (let [node (query-selector css-selector)]
    (add-event-listener node event-name (fn [e] (a/put! event-chan e)))))