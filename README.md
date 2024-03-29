# Opis Projekta
Projekt predstavlja sistem za upravljanje uporabnikov. Omogoča uporablja quarkus REST za upravljanje podatkov o uporabnikih. 
Omogoča pridobivanje, vstavljanje, posodabljanje in brisanje najemov avtomobilov iz sistema. Za shranjevanje podatkov se uporablja MongoDB baza. 
Sistem je zasnovan kot mikrostoritev, kar omogoča njegovo integracijo v večji sistem upravljanja vozil. Projektu je dodan tudi sporočilni posrednik rabbitmq
Ta projekt uporablja Quarkus, Supersonično Subatomsko Java Ogrodje.

Če želite izvedeti več o Quarkusu, obiščite spletno stran: [website](https://quarkus.io/).

## Zagon aplikacije v razvojnem načinu

Aplikacijo lahko zaženete v razvojnem načinu, ki omogoča živo kodiranje s pomočjo:

./mvnw compile quarkus:dev

## Čiščenje in izradnja projekta

mvn clean pakage

## Kontejnerizacija za docker in pogon aplikacije in baze v docker okolju

docker-compose up

## Vzpostavitev rabbitmq lokalno:

docker run -d --name rabbitmq-management -p 15672:15672 -p 5672:5672 rabbitmq:management 

## Swagger (docker):
http://localhost:8081/q/swagger-ui/#/User%20Service/get_user__id_

## Lastnosti
+ Povezava in trajno shranjevanje podatkov v MongoDB podatkovno bazo.
+ Izpisovanje dnevnikov delovanja (logov) za lažji nadzor delovanja.
+ Izvedba Unit testov za testiranje pravilnega delovanja končnih točk in repozitorija
+ kontejnerizacija za Docker (Dockerfile in docker-compose).
+ dodan sporočilni posrednik rabbitmq
+ OpenApi (Swagger)

