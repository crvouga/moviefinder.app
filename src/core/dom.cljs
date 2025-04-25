(ns core.dom
  (:require
   [clojure.core.async :as a]
   [core.frontend.console :as console]))

(defn query-selector! [css-selector]
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

(def document-body (.-body js/document))

(defn query-selector-chan! [css-selector]
  (let [result-chan (a/chan)
        mutation-chan (mutation-observer-chan! document-body {:childList true :subtree true})
        last-found (atom nil)]
    (a/go-loop []
      (let [_ (a/<! mutation-chan)]
        (let [element (query-selector! css-selector)]
          (when (and element (not= @last-found element))
            (reset! last-found element)
            (a/put! result-chan element))
          (when (and @last-found (not element))
            (reset! last-found nil)))
        (recur)))
    result-chan))



(let [mutation-chan (mutation-observer-chan! document-body {:childList true :subtree true})
      test-node-chan (query-selector-chan! "#test")]

  (defmethod console/cmd :test-add []
    (let [div (js/document.createElement "div")]
      (set! (.-id div) "test")
      (js/document.body.appendChild div)))

  (defmethod console/cmd :test-remove []
    (js/document.body.removeChild (js/document.querySelector "#test")))

  (a/go-loop []
    (let [_ (a/<! mutation-chan)]
      (println "mutation")
      (recur)))

  (a/go-loop []
    (let [node (a/<! test-node-chan)]
      (println "node" node)
      (recur))))



(defn query-chan! [css-selector]
  (let [result-chan (a/chan)
        observer (js/MutationObserver.
                  (fn [_]
                    (when-let [element (query-selector! css-selector)]
                      (a/put! result-chan element))))
        config #js {:childList true
                    :subtree true
                    :attributes true}]
    ;; Check immediately in case the element already exists
    (when-let [element (query-selector! css-selector)]
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
