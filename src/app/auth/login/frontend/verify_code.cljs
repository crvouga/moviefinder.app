(ns app.auth.login.frontend.verify-code
  (:require
   [app.frontend.screen :as screen]
   [app.frontend.toast :as toast]
   [clojure.core.async :refer [<! go-loop]]
   [core.program :as p]
   [core.result :as result]
   [core.ui.button :as button]
   [core.ui.text-field :as text-field]
   [core.ui.top-bar :as top-bar]))


(go-loop []
  (let [_ (<! (p/take! ::user-submitted-form))

        _ (p/put! [::set-request result/loading])

        code (-> (p/read!) ::code)

        phone-number (-> (p/read!) ::screen second :user/phone-number)

        req [:rpc/verify-code
             {:user/phone-number phone-number
              :verify-sms/code code}]

        res (<! (p/eff! [:rpc/send! req]))

        _ (p/put! [::set-request res])

        _ (when (result/ok? res)
            (p/put! [:login/authenticated res])
            (p/put! [:screen/push [:screen/profile]])
            (p/put! [:toaster/show (toast/info "Logged in")]))

        _ (when (result/error? res)
            (p/put! [:toaster/show (toast/error "Failed to login")]))]
    (recur)))


(go-loop []
  (let [msg (<! (p/take! ::inputted-code))]
    (p/put! [::set-code (-> msg second)])
    (recur)))


(p/reg-reducer ::set-code (fn [state msg] (assoc state ::code (second msg))))

(p/reg-reducer ::set-request (fn [state msg] (assoc state ::request (second msg))))


(defn- loading? [i]
  (-> i  ::request result/loading?))

(screen/register
 :screen/login-verify-code
 (fn [input]
   [:div.w-full.flex-1
    [top-bar/view {:top-bar/title "Verify Code"
                   :top-bar/on-back #(p/put! [:screen/clicked-link [:screen/login]])}]
    [:form.flex.flex-col.w-full.gap-6.p-6
     {:on-submit #(do (.preventDefault %) (p/put! [::user-submitted-form]))}
     [:p.text-lg "Enter the code we sent to " [:span.font-bold (-> input ::screen second :user/phone-number)]]
     [text-field/view
      {:text-field/label "Code"
       :text-field/value (-> input ::code)
       :text-field/type :text-field-type/number-pad
       :text-field/required? true
       :text-field/disabled? (loading? input)
       :text-field/on-change #(p/put! [::inputted-code %])}]
     [:div.w-full]
     [button/view
      {:button/type :button-type/submit
       :button/loading? (loading? input)
       :button/full? true
       :button/label "Verify Code"}]]]))