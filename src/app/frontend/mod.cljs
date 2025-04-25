(ns app.frontend.mod)

(defonce ^:private mods (atom #{}))

(defn reg [mod]
  (println "register" mod)
  (swap! mods conj mod))

(defn logic [i]
  (doseq [{:keys [mod/logic-fn]} @mods]
    (when (fn? logic-fn)
      (logic-fn i))))

(defn view [i]
  [:<>
   (for [{:keys [mod/view-fn mod/name]} @mods
         :when (fn? view-fn)]
     ^{:key name}
     [view-fn i])])