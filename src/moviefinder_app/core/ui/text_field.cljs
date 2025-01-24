(ns moviefinder-app.core.ui.text-field)

(defn- input-msg->value [e]
  (-> e .-target .-value))

(defn view [i]
  (let [on-change (:text-field/on-change i)
        value (:text-field/value i)
        label (:text-field/label i)
        placeholder (:text-field/placeholder i)
        disabled? (:text-field/disabled? i)
        required? (:text-field/required? i)]
    [:fieldset.flex.flex-col.gap-2
     [:label.font-bold.flex.flex-col.gap-2 label
      [:input.p-4.text-lg.bg-neutral-800.rounded.overflow-hidden
       {:type "text"
        :value value
        :placeholder placeholder
        :disabled disabled?
        :required required?
        :on-change #(-> % input-msg->value on-change)}]]]))