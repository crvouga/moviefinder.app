(ns core.dom
  (:require
   [clojure.core.async :as a]))

(defn query! [css-selector]
  (js/document.querySelector css-selector))

(defn mutation-observer-chan!
  "Creates a channel that receives notifications when DOM mutations occur.
   Returns a channel that will receive the MutationRecord objects."
  [target-node config]
  (let [result-chan (a/chan)
        observer (js/MutationObserver.
                  (fn [mutations _observer]
                    (a/put! result-chan mutations)))]
    (.observe observer target-node (clj->js config))
    result-chan))

(defn query-chan! [css-selector]
  (let [result-chan (a/chan)
        observer (js/MutationObserver.
                  (fn [_]
                    (when-let [element (query! css-selector)]
                      (a/put! result-chan element))))
        config #js {:childList true
                    :subtree true
                    :attributes true}]
    ;; Check immediately in case the element already exists
    (when-let [element (query! css-selector)]
      (a/put! result-chan element))
    (.observe observer js/document.body config)
    result-chan))

(defn event-chan
  "Creates a channel that listens for DOM events on a given DOM node.
   Returns a channel that will receive events when they occur."
  [^js dom-node event-name]
  (let [event-chan (a/chan)]
    (.addEventListener dom-node event-name (fn [event] (a/put! event-chan event)))
    event-chan))
