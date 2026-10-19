package notificationservice;

import java.util.*;
import java.util.concurrent.*;

public class NotificationDemo {
    public static void main(String[] args) {
        NotificationService service = new NotificationService(
            Map.of(
                ChannelType.EMAIL, new EmailChannel(),
                ChannelType.SMS, new SmsChannel(),
                ChannelType.PHONE, new PhoneChannel()
            )
        );

        Subscriber sam = new Subscriber(
            "s1", "sam@example.com", "+15550001111"
        );

        service.subscribe("aws", sam);
        service.updatePreference(
            "aws", "s1", Priority.HIGH,
            Set.of(ChannelType.EMAIL, ChannelType.SMS)
        );
        service.updatePreference(
            "aws", "s1", Priority.LOW,
            Set.of(ChannelType.EMAIL)
        );

        service.publish(new Notification(
            "n1", "aws", Priority.HIGH,
            "Service alert", "Your EC2 instance is unhealthy"
        ));
    }
}

enum Priority { HIGH, MEDIUM, LOW }
enum ChannelType { EMAIL, SMS, PHONE }
enum DeliveryStatus { PENDING, SENT, FAILED }

final class Subscriber {
    private final String id;
    private final String email;
    private final String phoneNumber;

    Subscriber(String id, String email, String phoneNumber) {
        this.id = id;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    String id() { return id; }
    String email() { return email; }
    String phoneNumber() { return phoneNumber; }
}

final class Notification {
    private final String id;
    private final String clientId;
    private final Priority priority;
    private final String title;
    private final String message;

    Notification(String id, String clientId, Priority priority,
                 String title, String message) {
        this.id = id;
        this.clientId = clientId;
        this.priority = priority;
        this.title = title;
        this.message = message;
    }

    String id() { return id; }
    String clientId() { return clientId; }
    Priority priority() { return priority; }
    String title() { return title; }
    String message() { return message; }
}

final class SubscriptionPreference {
    private final String clientId;
    private final String subscriberId;
    private final Map<Priority, Set<ChannelType>> channelsByPriority =
        new EnumMap<>(Priority.class);

    SubscriptionPreference(String clientId, String subscriberId) {
        this.clientId = clientId;
        this.subscriberId = subscriberId;
        for (Priority priority : Priority.values()) {
            channelsByPriority.put(priority, EnumSet.noneOf(ChannelType.class));
        }
    }

    synchronized void setChannels(Priority priority, Set<ChannelType> channels) {
        channelsByPriority.put(priority, EnumSet.copyOf(channels));
    }

    synchronized Set<ChannelType> channelsFor(Priority priority) {
        return EnumSet.copyOf(channelsByPriority.get(priority));
    }
}

final class DeliveryTask {
    private final Notification notification;
    private final Subscriber subscriber;
    private final ChannelType channelType;
    private DeliveryStatus status = DeliveryStatus.PENDING;

    DeliveryTask(Notification notification, Subscriber subscriber,
                 ChannelType channelType) {
        this.notification = notification;
        this.subscriber = subscriber;
        this.channelType = channelType;
    }

    Notification notification() { return notification; }
    Subscriber subscriber() { return subscriber; }
    ChannelType channelType() { return channelType; }
    DeliveryStatus status() { return status; }

    synchronized void markSent() { status = DeliveryStatus.SENT; }
    synchronized void markFailed() { status = DeliveryStatus.FAILED; }
}

interface NotificationChannel {
    ChannelType type();
    void send(DeliveryTask task) throws Exception;
}

final class EmailChannel implements NotificationChannel {
    public ChannelType type() { return ChannelType.EMAIL; }

    public void send(DeliveryTask task) {
        System.out.println(
            "EMAIL to " + task.subscriber().email() +
            " | " + task.notification().title()
        );
    }
}

final class SmsChannel implements NotificationChannel {
    public ChannelType type() { return ChannelType.SMS; }

    public void send(DeliveryTask task) {
        System.out.println(
            "SMS to " + task.subscriber().phoneNumber() +
            " | " + task.notification().message()
        );
    }
}

final class PhoneChannel implements NotificationChannel {
    public ChannelType type() { return ChannelType.PHONE; }

    public void send(DeliveryTask task) {
        System.out.println(
            "PHONE CALL to " + task.subscriber().phoneNumber()
        );
    }
}

final class NotificationService {
    private final Map<ChannelType, NotificationChannel> channels;
    private final Map<String, List<Subscriber>> subscribersByClient =
        new ConcurrentHashMap<>();
    private final Map<String, SubscriptionPreference> preferences =
        new ConcurrentHashMap<>();
    private final BlockingQueue<DeliveryTask> queue = new LinkedBlockingQueue<>();
    private final ExecutorService workers = Executors.newFixedThreadPool(3);

    NotificationService(Map<ChannelType, NotificationChannel> channels) {
        this.channels = Map.copyOf(channels);

        for (int i = 0; i < 3; i++) {
            workers.submit(this::processDeliveries);
        }
    }

    void subscribe(String clientId, Subscriber subscriber) {
        subscribersByClient
            .computeIfAbsent(clientId, ignored -> new CopyOnWriteArrayList<>())
            .add(subscriber);

        preferences.putIfAbsent(
            preferenceKey(clientId, subscriber.id()),
            new SubscriptionPreference(clientId, subscriber.id())
        );
    }

    void updatePreference(String clientId, String subscriberId,
                          Priority priority, Set<ChannelType> channels) {
        SubscriptionPreference preference = preferences.get(
            preferenceKey(clientId, subscriberId)
        );
        if (preference == null) {
            throw new IllegalArgumentException("Subscriber is not subscribed to client");
        }
        preference.setChannels(priority, channels);
    }

    void publish(Notification notification) {
        List<Subscriber> subscribers = subscribersByClient.getOrDefault(
            notification.clientId(), List.of()
        );

        for (Subscriber subscriber : subscribers) {
            SubscriptionPreference preference = preferences.get(
                preferenceKey(notification.clientId(), subscriber.id())
            );
            if (preference == null) continue;

            for (ChannelType type : preference.channelsFor(notification.priority())) {
                queue.offer(new DeliveryTask(notification, subscriber, type));
            }
        }
    }

    private void processDeliveries() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                DeliveryTask task = queue.take();
                NotificationChannel channel = channels.get(task.channelType());

                if (channel == null) {
                    task.markFailed();
                    continue;
                }

                try {
                    channel.send(task);
                    task.markSent();
                } catch (Exception e) {
                    task.markFailed();
                    System.err.println("Delivery failed for " + task.channelType());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private String preferenceKey(String clientId, String subscriberId) {
        return clientId + "#" + subscriberId;
    }
}