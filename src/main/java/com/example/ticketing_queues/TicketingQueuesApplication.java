package com.example.ticketing_queues;

import com.example.ticketing_queues.config.RabbitMqConfig;
import com.example.ticketing_queues.config.RabbitMqProperties;
import com.rabbitmq.client.ConnectionFactory;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync

public class TicketingQueuesApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketingQueuesApplication.class, args);
	}

	@Bean
	public ModelMapper mapper(){
		return new ModelMapper();
	}

	@Bean
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");
		connectionFactory.setUsername("guest");
		connectionFactory.setPassword("guest");
		return connectionFactory.getRabbitConnectionFactory();
	}

}
