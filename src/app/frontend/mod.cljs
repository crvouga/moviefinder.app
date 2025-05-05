(ns app.frontend.mod)

(defonce ^:private mods (atom {}))

(defn reg [mod]
  (println "register" mod)
  (swap! mods assoc (:mod/name mod) mod))

(defn logic [i]
  (doseq [{:keys [mod/logic]} (vals @mods)]
    (when (fn? logic)
      (logic i))))

(defn view [i]
  [:<>
   (for [[mod-name {:keys [mod/view]}] @mods
         :when (fn? view)]
     ^{:key mod-name}
     [view i])])