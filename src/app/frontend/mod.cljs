(ns app.frontend.mod)

(defonce ^:private mods (atom #{}))

(defn reg [mod]
  (println "register" mod)
  (swap! mods conj mod))

(defn logic [i]
  (doseq [{:keys [mod/logic-fn] :or {logic-fn identity}} @mods]
    (logic-fn i)))

(defn view [i]
  [:<>
   (for [{:keys [mod/view-fn mod/name] :or {view-fn identity}} @mods]
     ^{:key name}
     [view-fn i])])