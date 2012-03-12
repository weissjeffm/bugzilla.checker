(ns bugzilla.checker
  (:require [clj-http.client :as http])
  (:use 
        slingshot.slingshot
        [clojure.data.json :as json]))

(def req-id (atom 0))

(def ^:dynamic *url* "https://bugzilla.redhat.com/jsonrpc.cgi")
(def ^:dynamic *user* nil)
(def ^:dynamic *password*  nil)
(def ^:dynamic *open-statuses*
  ["NEW"
   "ASSIGNED"])

(defn get-bugs [bug-ids]
  (-> (http/post *url* 
           {:content-type :json
            :body (json/json-str
                   {:method "Bug.get_bugs"
                    :id (swap! req-id inc)
                    :params [{"Bugzilla_login" *user*
                              "Bugzilla_password" *password*
                              "ids" bug-ids}]})}) 
     :body read-json :result :bugs))

(defn fixed? [bug]
  (boolean
   (some (set *open-statuses*)
         (:status bug))))

(defn open-bz-bugs "Filters bug ids and returns as html links only those that are still open." [ & ids]
  (with-meta (fn [_]
               (for [bug (->> ids get-bugs (filter (complement fixed?)))]
                 (format "<a href='https://bugzilla.redhat.com/show_bug.cgi?id=%1$d'>%1$d: %2$s</a>"
                         (:id bug)
                         (:summary bug))))
    {:type :bz-blocker
     ::source `(~'open-bz-bugs ~@ids)}))

(defmethod print-method :bz-blocker [o ^java.io.Writer w]
  (print-method (::source (meta o)) w))