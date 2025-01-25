(ns core.ui.text-field)

(defn- input-msg->value [e]
  (-> e .-target .-value))

(def type->html-type
  {:text-field-type/keyboard "text"
   :text-field-type/number-pad "tel"})

(defn view [i]
  (let [on-change (:text-field/on-change i)
        value (:text-field/value i)
        label (:text-field/label i)
        placeholder (:text-field/placeholder i)
        disabled? (:text-field/disabled? i)
        type (-> i :text-field/type type->html-type (or "text"))
        required? (:text-field/required? i)]
    [:fieldset.flex.flex-col.gap-2
     [:label.font-bold.flex.flex-col.gap-2 label
      [:input.p-4.text-lg.bg-neutral-800.rounded.overflow-hidden.font-normal.min-w-0.max-w-full.flex-basis
       {:type type
        :value value
        :placeholder placeholder
        :disabled disabled?
        :required required?
        :on-change #(-> % input-msg->value on-change)}]]]))