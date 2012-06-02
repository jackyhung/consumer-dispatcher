#The Consumer Dispatcher Project

Consumer Dispatcher works as a proxy between [RabbitMQ](http://www.rabbitmq.com/) and your consumers. Let you scale out and maintain consumers easily.

It provides:
- managing consumers of different queues separately
- serving queues for multiple sites with one instance
- avoid blocking consuming queues
- adding/reducing number of consumers on the fly by clicking a button
- load balance between consumers
- purging queues by clicking a button
- separating bad jobs from good ones in a queue and logging bad jobs to files
- statistics
- master - slave modle
- adding more instance on the fly
- etc.

It can work with all http-based interfaces written in any language. 

[The NetCircle](www.thenetcircle.com) has been using it for more than 18 months. It helps us to consume more than 6 million(the amount depends on your businsess type. Consumer Dispatcher wont be your bottleneck) jobs per day with around 25M memory and 2%cpu usage.

## Links

- [What is Consumer Dispatcher and Why](https://github.com/jackyhung/consumer-dispatcher/wiki/What-is-Consumer-Dispatcher-and-Why) 

- [How it works](https://github.com/jackyhung/consumer-dispatcher/wiki/How-it-works)

- [How to use it](https://github.com/jackyhung/consumer-dispatcher/wiki/How-to-use-it)

- [How to write my consumder code](https://github.com/jackyhung/consumer-dispatcher/wiki/How-to-write-my-Consumer-Code)

- [Managing & Monitoring](https://github.com/jackyhung/consumer-dispatcher/wiki/Managing-&-Monitoring)

- [Run in master-slave way](https://github.com/jackyhung/consumer-dispatcher/wiki/Run-in-master-slave-way)

- [Wiki](https://github.com/jackyhung/consumer-dispatcher/wiki)

- email: jackyhung81@gmail.com

- weibo: http://www.weibo.com/jackyhung

- company: http://www.thenetcircle.com

## Getting Started
 
- download the [jar file](https://raw.github.com/jackyhung/consumer-dispatcher/master/downloads/consumerdispatcher-0.1.1-jar-with-dependencies.jar) 

- follow the steps described [here](https://github.com/jackyhung/consumer-dispatcher/wiki/How-to-use-it#wiki-howtorun)

## Developer Information

- [compile, package and eclipse project](https://github.com/jackyhung/consumer-dispatcher/wiki/How-to-use-it#wiki-compile)

- Consumer Dispatcher is setup to build using [Maven](http://maven.apache.org/)

- You need JDK 1.6 + to build and run Consumer Dispatcher.

- Apache License, Version 2.0

##There definitely are loooooooot of room to improve Consumer Dispatcher. Any suggestion would be appreciated . Thank you for your support!