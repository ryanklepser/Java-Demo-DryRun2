package ca.uhn.fhir.jpa.batch.config;

/*-
 * #%L
 * HAPI FHIR JPA Server - Batch Task Processor
 * %%
 * Copyright (C) 2014 - 2022 Smile CDR, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;


@Configuration
public class NonPersistedBatchConfigurer {
	@Autowired
	@Qualifier("hapiTransactionManager")
	private PlatformTransactionManager myHapiPlatformTransactionManager;

	@Autowired
	@Qualifier(BatchConstants.JOB_LAUNCHING_TASK_EXECUTOR)
	private TaskExecutor myTaskExecutor;

	@Bean
	public DataSource batchDataSource() {
		return new EmbeddedDatabaseBuilder()
			.setType(EmbeddedDatabaseType.H2)
			.addScript("/org/springframework/batch/core/schema-h2.sql")
			.build();
	}

	@Bean
	public PlatformTransactionManager batchTransactionManager() {
		return new DataSourceTransactionManager(batchDataSource());
	}

	@Bean
	public JobRepository jobRepository() throws Exception {
		JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
		factory.setDataSource(batchDataSource());
		factory.setTransactionManager(batchTransactionManager());
		factory.afterPropertiesSet();
		return factory.getObject();
	}

	@Bean
	public JobExplorer jobExplorer() throws Exception {
		JobExplorerFactoryBean factory = new JobExplorerFactoryBean();
		factory.setDataSource(batchDataSource());
		factory.setTransactionManager(batchTransactionManager());
		factory.afterPropertiesSet();
		return factory.getObject();
	}

	@Bean
	public JobLauncher jobLauncher(JobRepository jobRepository) throws Exception {
		TaskExecutorJobLauncher launcher = new TaskExecutorJobLauncher();
		launcher.setTaskExecutor(myTaskExecutor);
		launcher.setJobRepository(jobRepository);
		launcher.afterPropertiesSet();
		return launcher;
	}

	public PlatformTransactionManager getTransactionManager() {
		return myHapiPlatformTransactionManager;
	}
}
