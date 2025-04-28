(ns core.dom
  (:require
   [clojure.core.async :as a]
   [core.console :as console]))

(def document-body (.-body js/document))

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

(defn watch-query-selector-chan! [css-selector]
  (let [result-chan (a/chan)
        mutation-chan (mutation-observer-chan! document-body {:childList true :subtree true})]
    (a/go-loop []
      (let [_ (a/<! mutation-chan)]
        (when-let [element (query-selector! css-selector)]
          (a/put! result-chan element))
        (recur)))
    result-chan))


(defn event-chan
  "Creates a channel that listens for DOM events on a given DOM node.
   Returns a channel that will receive events when they occur."
  [dom-node event-name]
  (let [event-chan (a/chan)]
    (when dom-node
      (.addEventListener dom-node event-name (fn [event] (a/put! event-chan event))))
    event-chan))

(defn watch-event-chan!
  ([css-selector event-name]
   (watch-event-chan! css-selector event-name identity))

  ([css-selector event-name transducer]
   (let [result-chan (a/chan transducer)
         node-chan (watch-query-selector-chan! css-selector)]
     (a/go-loop []
       (let [node (a/<! node-chan)
             event-chan (event-chan node event-name)]
         (a/go-loop []
           (let [event (a/<! event-chan)]
             (a/put! result-chan event)
             (recur)))
         (recur)))
     result-chan)))


(defn animation-end-once [css-selector]
  (let [result-chan (a/chan)
        node-chan (watch-query-selector-chan! css-selector)]
    (a/go-loop []
      (let [node (a/<! node-chan)
            event-chan (event-chan node "animationend")]
        (a/go-loop []
          (let [event (a/<! event-chan)]
            (a/put! result-chan event)
            (a/close! result-chan)
            (recur)))
        (recur)))
    result-chan))


;; 
;; 
;; 
;; 
;; 
;; 

(let [mutation-chan (mutation-observer-chan! document-body {:childList true :subtree true})
      test-node-chan (watch-query-selector-chan! "#test")]

  (defmethod console/cmd :test-add []
    (let [div (js/document.createElement "div")]
      (set! (.-id div) "test")
      (js/document.body.appendChild div)))

  (defmethod console/cmd :test-remove []
    (js/document.body.removeChild (js/document.querySelector "#test")))

  (a/go-loop []
    (let [_ (a/<! mutation-chan)]
      #_(println "mutation")
      (recur)))

  (a/go-loop []
    (let [_ (a/<! test-node-chan)]
      #_(println "node" node)
      (recur))))

