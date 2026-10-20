package notificationservice;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.LinkedBlockingQueue;

/*
    Requirements
    1. Multiple clients send notifications user our System.
    2. Notifications sent on multiple channels
    3. Notifications will be sent to subscribers based on their preferences (priority -> channels)
    4. Asynchronous notification delivery
    5. Get channels at start of the service

    Entities
    NotificationService - orchestrator - Map<ChannelType, Channel>, map<clientId, List<Subscriber>>
                            Map<clientId+subsId, Map<priority, List<Channel>>> subscriberPreferences
                            Queue<DeliveryTask>, Worker
    Notification - clientId, priority, message
    Subscriber - subscriberId, name, email, phonenumber
    SubscriberPreferences - clientId, subscriberId, Map<priority, List<Channel>> preferences
    DeliveryTask - Notification, Channel, Subscriber


public class NotificationService {
    private final Map<ChannelType, Channel> channels;
    private final Map<String, List<Subscriber>> subscribers;
    private final Map<String, Map<Priority, List<Channel>>> subscriberPreferences;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Queue<DeliveryTask> taskQueue;

    public NotificationService(Map<ChannelType, Channel> channels) {
        this.channels = channels;
        subscribers = new HashMap<>();
        subscriberPreferences = new HashMap<>();
        taskQueue = new LinkedBlockingQueue<>();
        worker.submit(this::executeTasks);
    }

    private void executeTasks() {
        volatile boolean running = true;
        while(running){
            //pick task
            try {
                DeliveryTask task = taskQueue.poll();
                try {
                    task.channel.send(task.notification);
                }
                catch (Exception e){
                    //log and retry
                }
            }
            catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }
    }

    //Set subscriber

    //update preferences

    //send notification
    public void send(Notification notification){
        //validate clientId
        // get the subscribers for the client
        List<Subscriber> users = subscribers.get(notification.clientId);
        // get the subscriber preferences
        //create DeliveryTask for each preference and add to queue

        for(Subscriber subs : subscribers){
            String key = notification.clientId + subs.subsId;
            List<SubscriberPreference> preferences = subscriberPreferences.get(key);
            for(SubscriberPreference preference : preferences){
                if(preference.priority == notification.priority){
                    for(Channel channel : preference.channels)
                    DeliveryTask task = new DeliveryTask(notification,channel);
                    taskQueue.offer(task);
                }
            }
        }
    }

}

class Notification{
    String clientId;
    Priority priority;
    String message;
}

class DeliveryTask{
    Channel channel;
    Notification notification;
    Subscriber subscriber;
}
class SubscriberPreference{
    String clientId, subsId;
    Priority priority;
    List<Channel> channels;
}

class Subscriber{
    String subsId;
}

interface Channel{
    public void send(Notification notification);
}
class EmailChannel implements Channel{
    @Override
    public void send(Notification notification) {
        //send
    }
}
class SMSChannel implements Channel{
    @Override
    public void send(Notification notification) {
        //send
    }
}
class Notification{
    String clientId;
    Priority priority;
    String message;
}
enum ChannelType{
    EMAIL,
    SMS,
    PUSH
}
enum Priority{
    LOW,
    MEDIUM,
    HIGH
}


 */