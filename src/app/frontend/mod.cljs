(ns app.frontend.mod)

(defonce ^:private mods (atom {}))

(defn reg [mod]
  (println "register" mod)
  (swap! mods assoc (:mod/name mod) mod))

(defn logic [i]
  (doseq [{:keys [mod/logic-fn]} (vals @mods)]
    (when (fn? logic-fn)
      (logic-fn i))))

(defn view [i]
  [:<>
   (for [[mod-name {:keys [mod/view-fn]}] @mods
         :when (fn? view-fn)]
     ^{:key mod-name}
     [view-fn i])])