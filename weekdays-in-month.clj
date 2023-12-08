#!/usr/bin/env bb

; example:
; ./weekdays-in-month.clj --month 12 --append ", worked 8 hours in a script for creating an invoice"

(require '[clojure.string :as str])
(require '[babashka.cli :as cli])
(import '[java.time DayOfWeek LocalDate YearMonth])
(import '[java.time.format DateTimeFormatter])

(def dictionary
  {:pt {:months [nil "Janeiro" "Fevereiro" "Março" "Abril" "Maio" "Junho" "Julho" "Agosto" "Setembro" "Outubro" "Novembro" "Dezembro"]
        :days-in-week ["Domingo" "Segunda" "Terça" "Quarta" "Quinta" "Sexta" "Sábado"]}})

(defn day-of-week-int
  [^LocalDate date]
  (.getValue (.getDayOfWeek date)))

(defn weekday?
  [^LocalDate date]
  (<= (day-of-week-int date) (.getValue DayOfWeek/FRIDAY)))

; babashka does not come with locales other than :en, so we have to build a custom one
(defn formatted-custom
  [language ^LocalDate date]
  (let [dict (dictionary language)
        days-in-week (dict :days-in-week)
        months (dict :months)]
    (str (days-in-week (day-of-week-int date)) ", " (.getDayOfMonth date) " de " (months (.getMonthValue date)) " de " (.getYear date))))

(defn formatted
  [language ^LocalDate date]
  (case language
    :en (.format date (DateTimeFormatter/ofPattern "EEEE, MMMM d, yyyy"))
    (formatted-custom language date)))

(defn str-log
  [{:keys [month year language]}]
  (->> (range 1 (inc (.lengthOfMonth (YearMonth/of year month))))
       (map #(LocalDate/of year month %))
       (filter weekday?)
       (map (partial formatted language))))

(def cli-options {:month {:coerce :long}
                  :year {:default (.getYear (LocalDate/now)) :coerce :long}
                  :language {:default :en :coerce :keyword}
                  :append {:coerce :string}})

(let [{:keys [append] :as args} (cli/parse-opts *command-line-args* {:spec cli-options})]
  (println
   (->> args
        str-log
        (map #(str % append))
        (str/join "\n"))))
