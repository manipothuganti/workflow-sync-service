### workflow-sync-service


### API Documentation

The API is documented by updating `openapi.yaml`, which resides in the `src/main/resources/static` folder.

To update `openapi.yaml`, use Swagger Editor. There are two ways to access the editor:

1. http://editor.swagger.io/#/
   - OR -
2. Run a Docker image of the Swagger Editor and access it using localhost

   To run Docker image: https://hub.docker.com/r/swaggerapi/swagger-editor/

The `openapi.yaml` file can be imported or copied/pasted into the editor. (Save changes locally)

To view and interact with the API after editing, use Swagger UI. There are two ways to access the UI:

1. Download the UI: https://swagger.io/swagger-ui/
   - OR -
2. Run a Docker image of the Swagger UI and access it using localhost

   To run Docker image: https://hub.docker.com/r/swaggerapi/swagger-ui/

The `openapi.yaml` file is served at `http://localhost:8080/workflow-sync-service/openapi.yaml`. To access while the application
is running locally, copy/paste this URL into Swagger UI to see and interact with the API documentation.

### Calling the Sample API

* `curl -X PUT -H "Content-Type: application/json"  -d '{ "message": "Greetings", "user_name": "Program" }' http://localhost:8080/workflow-sync-service/api/v1/messages/1`
* `curl -X PUT -H "Content-Type: application/json"  -d '{ "message": "Hello", "user_name": "World" }' http://localhost:8080/workflow-sync-service/api/v1/messages/2`
* `curl -H "Content-Type: application/json" http://localhost:8080/workflow-sync-service/api/v1/messages`
* `curl -X PUT -H "Content-Type: application/json"  -d '{ "message": "Greetings", "user_name": "Program", "id":"4" }' -v http://localhost:8080/workflow-sync-service/api/v1/messages/3`
* `curl -v -H "Content-Type: application/json" -H "X-Chaos-Uri: /workflow-sync-service/api/v1/messages" -H "X-Chaos-Status-Code: 500" -H "X-Chaos-Latency: 5000" http://localhost:8080/workflow-sync-service/api/v1/messages`

### Environment Variables / Startup Params

* `SPRING_PROFILES_ACTIVE` (This will drive the configuration of the running instance)
* `SPRING_CONFIG_LOCATION` (Sets additional config. Alternative to above)
* logging.config=<path to logback.xml> (optional)
* logging.path=<path to directory for logs> (optional)

### Pipeline Jobs

* `(pipeline/seed_vt.dsl)` - Definitions of the JobDsl jobs for Vantam pipelines
* `(pipeline/pipeline_dev_qa.groovy)` - Workflow pipeline to build and deploy your application in dev and qa
  environments on AWS
* `(pipeline/pipeline_uat_prod.groovy)` - Workflow pipeline to deploy your application on uat and production
  environments on AWS
* `(pipeline/seed_k8s.dsl)` - Definitions of the JobDsl jobs for Kubernetes pipelines
* `(pipeline/01_commit.groovy)` - Build phase of kubernetes pipeline.
* `(pipeline/02_nonprod.groovy)` - EKS deployments for dev, int and (with approval) qa.
* `(pipeline/03_prod.groovy)` - EKS deployment to prod. Preconfigured after a QA deployment. Perform a Jenkins rebuild
  on a particular version to run a deployment.

### Create a Seed Job on Jenkins

You can create the seed job manually by following those instructions in Jenkins:

* Create Folder under a parent folder in Jenkins, New Item → Item Name: `<projectFolder>`, click 'Folder' radio button (
  see: set-up-jobs.dsl:projectFolder)
* Create Job, New Item → Item Name: `seed`, click 'Freestyle project' radio button
* General → Discard old builds → Max # of builds to keep: '5'
* Source Code Management → Git Repositories URL: `git@github.com:discoveryinc-cs/<projectFolder>`
* Build Triggers → Poll SCM schedule: `H/5 * * * *`
* Add Build Steps, Process Job DSLs → DSL Scripts: `pipeline/seed_vt.dsl` (for deploying to EC2 instances)
  or `pipeline\seed_k8s.dsl` for deployment to Kubernetes.
