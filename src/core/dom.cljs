(ns core.dom
  (:require
   [clojure.core.async :as a]
   [core.frontend.console :as console]))

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

(defn watch-event-chan! [css-selector event-name transducer]
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
    result-chan))

(defn- cleanup-node! [node event-name handler]
  (when node
    (.removeEventListener node event-name handler)))

(defn- setup-node! [node event-name out-chan]
  (let [handler (fn [event]
                  (when (not (a/closed? out-chan))
                    (a/put! out-chan event)))]
    (.addEventListener node event-name handler)
    (fn [] (cleanup-node! node event-name handler))))

#_(defn watch-dom-events
    "Watches for DOM nodes matching selector and puts events on channel.
   Automatically cleans up when nodes are removed or channel is closed.
   Returns just the channel (cleanup is automatic).
   
   Args:
   - selector: CSS selector string
   - event-name: DOM event name to listen for
   - xform: transducer to apply to events
   - opts: optional map with :target (defaults to document.body)"
    [selector event-name xform & [{:keys [target]}]]
    (let [out-chan (a/chan (a/sliding-buffer 1) xform)
          target-node (or target js/document.body)
          cleanup-fns (atom {}) ; {node-id -> cleanup-fn}
          observer (js/MutationObserver.
                    (fn [mutations]
                      (when (not (a/closed? out-chan))
                        (doseq [mutation mutations]
                          (when (or (.-addedNodes mutation) (.-removedNodes mutation))
                            (let [existing-nodes (array-seq (js/document.querySelectorAll selector))]
                              ;; Cleanup removed nodes
                              (doseq [[node _] @cleanup-fns]
                                (when (not (some #(= % node) existing-nodes))
                                  (when-let [cleanup (get @cleanup-fns node)]
                                    (cleanup)
                                    (swap! cleanup-fns dissoc node))))

                              ;; Setup new nodes
                              (doseq [node existing-nodes]
                                (when-not (contains? @cleanup-fns node)
                                  (let [cleanup (setup-node! node event-name out-chan)]
                                    (swap! cleanup-fns assoc node cleanup))))))))))]

      ;; Initial setup
      (let [nodes (array-seq (js/document.querySelectorAll selector))]
        (doseq [node nodes]
          (let [cleanup (setup-node! node event-name out-chan)]
            (swap! cleanup-fns assoc node cleanup))))

      ;; Start observing
      (.observe observer target-node #js {:childList true :subtree true})

      ;; Setup channel cleanup
      (a/go
        (a/<! out-chan) ; Wait for channel to close
        (.disconnect observer)
        (doseq [[_ cleanup] @cleanup-fns]
          (cleanup)))

      out-chan)) ; Return just the channel

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

