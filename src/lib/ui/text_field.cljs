(ns lib.ui.text-field)

(defn- input-msg->value [e]
  (-> e .-target .-value))

(def type->html-type
  {:text-field/type-keyboard "text"
   :text-field/type-num-pad "tel"})

(defn view [i]
  [:fieldset.flex.flex-col.gap-2
   {:class (-> i :text-field/class)}
   [:label.font-bold.flex.flex-col.gap-2 (-> i :text-field/label)
    [:input.p-4.text-lg.bg-neutral-800.rounded.overflow-hidden.font-normal.min-w-0.max-w-full.flex-basis.border-2
     {:type (-> i :text-field/type type->html-type (or "text"))
      :value (-> i :text-field/value (or ""))
      :placeholder (-> i :text-field/placeholder)
      :disabled (-> i :text-field/disabled? (or false))
      :required (-> i :text-field/required? (or false))
      :on-change (fn [e]
                   (let [value (input-msg->value e)
                         on-change (-> i :text-field/on-change)]
                     (on-change value)))}]]])