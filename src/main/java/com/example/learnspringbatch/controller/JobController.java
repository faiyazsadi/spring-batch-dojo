package com.example.learnspringbatch.controller;

import com.example.learnspringbatch.listener.ChunkEventListener;
import com.example.learnspringbatch.listener.CustomerJobExecutionListener;
import com.example.learnspringbatch.service.ChunkService;
import com.example.learnspringbatch.service.JobSchedulerService;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@RestController
@RequestMapping("/jobs")
@AllArgsConstructor
public class JobController {
    @Autowired
    @Qualifier("CustomJobLauncher")
    private JobLauncher jobLauncher;

    private Job job;

    private final ChunkEventListener chunkEventListener;
    private final ChunkService chunkService;
    private RestTemplate restTemplate;
    private CustomerJobExecutionListener customerJobExecutionListener;
    private final JobSchedulerService jobSchedulerService;

    @PostMapping("/import")
    public void importCsvToDbJob() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis()).toJobParameters();

        try {
            jobLauncher.run(job, jobParameters);
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/import-customers")
    public ResponseEntity<Long> importCsvToDBJob(@RequestBody String fileName) throws Exception {
        return jobSchedulerService.importCustomers(fileName);
    }

    @PostMapping("/stress-test")
    public void stressTest(@RequestBody String count) {
        String targetURL = "http://localhost:8000/jobs/import-customers";

        for(int i = 0; i < Integer.parseInt(count); i++) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);

            HttpEntity<String> request = new HttpEntity<>("customers-10k.csv", headers);
//            HttpEntity<String> request = new HttpEntity<>("customers-1m.csv", headers);
            restTemplate.postForObject(targetURL, request, String.class);
        }
    }

    @GetMapping("/status/{job_execution_id}")
    public Double getJobStatus(@PathVariable Long job_execution_id) {
        Integer chunkSize = chunkService.getChunkSize();
        Long datasetSize = customerJobExecutionListener.getDatasetLen(job_execution_id);

        Double reqChunk = Double.valueOf(datasetSize) / chunkSize;
        Long processedChunk = chunkEventListener.getProcessedChunkCount(job_execution_id);

        return (Double.valueOf(processedChunk) / reqChunk) * 100;
    }
}
