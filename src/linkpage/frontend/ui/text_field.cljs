(ns linkpage.frontend.ui.text-field)

(defn- input-msg->value [e]
  (-> e .-target .-value))

(defn view [i]
  (let [on-change (:text-field/on-change i)
        value (:text-field/value i)
        label (:text-field/label i)
        disabled? (:text-field/disabled? i)
        required? (:text-field/required? i)]
    [:fieldset
     [:label label
      [:input {:type "text"
               :value value
               :disabled disabled?
               :required required?
               :on-change #(-> % input-msg->value on-change)}]]]))