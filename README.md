# ***DROM PARSER ENGINE IN JAVA SPRING***
![logo](https://sun9-26.userapi.com/lzn3DyiOwqzga74d-sVF8SlVIOC-iO-AV_TBcg/-Yklk42ySOw.jpg)
## The redesigned engine of [my former parser](https://github.com/DanchiEllo/drom-parser-chatbot-ru_doesnt_work) in java Spring(Maven). This is the server part of the parser to which requests will be sent from your client part.

---

>This option already has attribute and tag settings in the [ParserAttributeSettings](https://github.com/DanchiEllo/server-part-parser-basics-drom/blob/master/src/main/java/com/dromparser/DromParser/ParserAttributeSettings.java) file.

---

**How to use:**

  * Upload to the server, run, and send requests to it from the client side with defined headers and the request body 
  >(with very frequent requests to drom.ru (approximately 36 requests per second) the site can block your server's Ip for a while).

---
**Customization:**

* Configure your database, by default, the h2 database is used in RAM. You need to redo it for your MySQL and you may have to register your dialects.
  
* It is natural to make the first test request
 >(example of the request body) 
>```JSON
>{
>"URL": "https://auto.drom.ru/all/"
>}
>```


  and if you see normal data, then everything works.

* That's it!

---

## **important!** 
This is normal if in **SOME** records you see in some fields, for example, year 
>(you need to handle such situations when there are a lot of words in the name of a car)

 or publicationTime
 
>(basically null puts on only just published cars)

 the null value and in the console the error message of these functions. 
 
 However, if you constantly see the null value in certain fields at different urls in the request body, then you need to change the attribute in the settings [ParserAttributeSettings](https://github.com/DanchiEllo/server-part-parser-basics-drom/blob/master/src/main/java/com/dromparser/DromParser/ParserAttributeSettings.java) file

---

**Its functionality:**
* Collects information about cars, namely: brand, model, year of manufacture, price in rubles, mileage, engine capacity, horsepower, ad location, ad photo, publication time, ad url.

* Saves cars to the database
  >(does not save cars whose urls are already present in the database in order not to duplicate entries)

* Sends in json format with a get request all cars available in the database
  >(you can add and register get requests for certain parameters)
