package clonegod.mongodb.config;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

@Configuration
@PropertySource(value= "classpath:/mongo.properties")
@Profile({ "default"})
@EnableMongoAuditing
public class LocalMongoConfig {

    @Autowired
    Environment env;

    /**
     * 配置MongoClient的详细参数
     * 
     * @return
     * @throws UnknownHostException
     */
    @Bean
    public MongoClient mongoClient() throws UnknownHostException {
    	// 生产环境需要配置多个地址 --- sharded cluster 则配置mongos的地址
    	List<ServerAddress> addresses = new ArrayList<>();
    	ServerAddress  address = new ServerAddress(env.getProperty("mongo.server"), 
    			Integer.parseInt( env.getProperty("mongo.port")));
    	addresses.add(address);
    	
    	List<MongoCredential> credentialsList = new ArrayList<>();
    	MongoCredential credential = MongoCredential.createPlainCredential(
    			env.getProperty("mongo.userName"), 
    			env.getProperty("mongo.databaseName"), 
    			env.getProperty("mongo.password").toCharArray());
    	credentialsList.add(credential);
    	
    	/**
    	 * Best practices in configuration 
    	 * 	 see -> http://infinitereusablecomponents.blogspot.com/2015/09/spring-data-mongodb-best-practices.html 
    	 */
        MongoClientOptions.Builder builder =  new MongoClientOptions.Builder();
        builder.connectionsPerHost(100); // 客户端连接池最多可以建立的连接数
        builder.threadsAllowedToBlockForConnectionMultiplier(5); // 连接池耗尽时，允许阻塞等待可用连接的线程个数-5表示乘积因子，100 * 5 最多500个线程阻塞等待线程池的连接
        builder.connectTimeout(10*1000 );
        builder.maxWaitTime(1000 * 60 * 2);
        builder.writeConcern(WriteConcern.JOURNALED); // 配置写操作的安全级别
        builder.readPreference(ReadPreference.secondaryPreferred()); // 优先在secondary上查询，提供最终一致性的保证
        MongoClientOptions options = builder.build();
        
        MongoClient mongoClient = new MongoClient(
        		addresses, 
        		/*credentialsList,*/
        		options);
        return mongoClient;
    }

    @Bean
    public MongoDbFactory mongoDbFactory() throws UnknownHostException {
        MongoDbFactory mongoDbFactory = new SimpleMongoDbFactory(mongoClient(), 
        		env.getProperty("mongo.databaseName"));
        return mongoDbFactory;

    }

//    @Bean
//    public MongoTemplate mongoTemplate() throws UnknownHostException {
//        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory());
//        return mongoTemplate;
//    }
    
    /**
     * Remove Unnecessary _Class Field in mongoDB document。
     * This will help us to reduce the document size, since _class will be there in all the documents and consume space.
     * 
     * Who add _class field in the document?
     * Spring MongoDB by default includes _class field pointing to the entity's fully-qualified class name in the document 
     *  as some kind of hint about what type to instantiate actually. 
     */
    @Bean
    public MongoTemplate mongoTemplate() throws UnknownHostException {
        MappingMongoConverter mappingMongoConverter =  
        		new MappingMongoConverter(
	        		new DefaultDbRefResolver(mongoDbFactory()), 
	        		new MongoMappingContext());
        
        mappingMongoConverter.setTypeMapper(new DefaultMongoTypeMapper(null));
        
        return new MongoTemplate(mongoDbFactory(), mappingMongoConverter );
    }



}