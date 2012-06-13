(ns ring.middleware.json-params
  (:import [com.fasterxml.jackson.core JsonParseException])
  (:require [cheshire.core :as json]
            [clj-stacktrace.repl :as stacktrace]))

(defn- json-request?
  [req]
  (if-let [#^String type (:content-type req)]
    (not (empty? (re-find #"^application/(vnd.+)?json" type)))))

(defn wrap-json-params [handler]
  (fn [req]
    (if-let [body (and (json-request? req) (:body req))]
      (let [bstr (slurp body)
            req* (assoc req :body bstr)
            json-params (try (json/parse-string bstr) (catch JsonParseException e (stacktrace/pst-str e)))
            req* (if (string? json-params)
                   (assoc req* :exception json-params)
                   (assoc req*
                     :json-params json-params
                     :params (merge (:params req) json-params)))]
        (handler req*))
      (handler req))))
