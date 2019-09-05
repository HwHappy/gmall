package com.atguigu.gmall.payment;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPaymentApplicationTests {

	@Test
	public void contextLoads() {
	}


	/**
	 * 生产消息的方法
	 * @throws JMSException
	 */
	@Test
	public  void mqTest() throws JMSException {

		//创建工厂
		ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("tcp://192.168.233.129:61616");
		Connection connection = activeMQConnectionFactory.createConnection();
		connection.start();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Queue queue = session.createQueue("atguigu");
		MessageProducer producer = session.createProducer(queue);

		ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
		activeMQTextMessage.setText("想吃鸡腿");

		producer.send(activeMQTextMessage);

		producer.close();
		session.close();
		connection.close();


	}

	/**
	 * 消费消息的方法
	 * @throws JMSException
	 */
	@Test
	public void test() throws JMSException {
		ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("tcp://192.168.233.129:61616");
		Connection connection = activeMQConnectionFactory.createConnection();
		connection.start();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Queue queue = session.createQueue("atguigu");
		MessageConsumer consumer = session.createConsumer(queue);
		consumer.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message message) {
				if (message instanceof  TextMessage){
					try {
						String text = ((TextMessage) message).getText();
						System.out.println(text);
					} catch (JMSException e) {
						e.printStackTrace();
					}

				}
			}
		});


	}

}
