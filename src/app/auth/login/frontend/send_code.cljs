(ns app.auth.login.frontend.send-code
  (:require [app.frontend.screen :as screen]
            [app.frontend.toast :as toast]
            [clojure.core.async :refer [<! go-loop]]
            [core.program :as p]
            [core.result :as result]
            [core.ui.button :as button]
            [core.ui.text-field :as text-field]
            [app.auth.login.frontend.shared :as shared]))


(go-loop []
  (let [_ (<! (p/take! ::user-submitted-form))

        _ (println "user-submitted-form")

        _ (p/put! [::set-request result/loading])

        _ (println "set-request")

        phone-number (-> (p/read!) ::phone-number)

        _ (println "phone-number")

        req [:rpc/send-code
             {:user/phone-number phone-number}]

        _ (println "req" req)

        res (<! (p/eff! [:rpc/send! req]))

        _ (println "res" res)

        _ (p/put! [::set-request res])

        _ (println "set-request")

        _ (when (result/ok? res)
            (p/put! [:screen/push [:screen/login-verify-code {:user/phone-number phone-number}]])
            (p/put! [:toaster/show (toast/info "Code sent")]))

        _ (when (result/error? res)
            (p/put! [:toaster/show (toast/error (-> res :error/message (or "Failed to send code")))]))]
    (recur)))


(go-loop []
  (let [msg (<! (p/take! ::inputted-phone-number))]
    (p/put! [::set-phone-number (-> msg second)])
    (recur)))

(p/reg-reducer ::set-phone-number (fn [state msg] (assoc state ::phone-number (second msg))))

(p/reg-reducer ::set-request (fn [state msg] (assoc state ::request (second msg))))


(defn- loading? [i]
  (-> i ::request result/loading?))

(screen/register
 :screen/login
 (fn [input]
   [shared/view-layout "Login with SMS"
    [:form.flex.flex-col.w-full.gap-6
     {:on-submit #(do (.preventDefault %) (p/put! [::user-submitted-form]))}
     [text-field/view
      {:text-field/label "Phone Number"
       :text-field/value (-> input ::phone-number)
       :text-field/required? true
       :text-field/type :text-field-type/number-pad
       :text-field/disabled? (loading? input)
       :text-field/on-change #(p/put! [::inputted-phone-number %])}]
     [:div.w-full]
     [button/view
      {:button/type :button-type/submit
       :button/full? true
       :button/loading? (loading? input)
       :button/label "Send Code"}]]]))
