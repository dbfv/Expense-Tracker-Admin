---
status: investigating
trigger: "Investigate issue: Cloudinary 401 error (invalid cloud_name)"
created: 2026-04-09T00:00:00.000Z
updated: 2026-04-09T00:00:00.000Z
---

## Current Focus
hypothesis: Gathering initial evidence for Cloudinary 401 error (invalid cloud_name)
test: Review configuration, credential loading, and upload logic
expecting: Identify where cloud_name is set and how it is passed to Cloudinary SDK
next_action: Gather initial evidence from config and upload code

## Symptoms
expected: Images should upload to Cloudinary successfully using signed upload with credentials from BuildConfig/local.properties.
actual: Upload fails with 401 error: "Invalid cloud_name du362f923". Stack trace shows error from Cloudinary Android SDK.
errors: 401 error, message: "Invalid cloud_name du362f923". Stack trace from DefaultRequestProcessor, UploaderStrategy, Uploader, MediaManager, AndroidJobStrategy.
reproduction: Attempt to upload images from the app using the current signed upload logic.
started: Started after switching to signed upload and using BuildConfig/local.properties for credentials. Previously worked with unsigned preset.

## Eliminated

## Evidence

## Resolution
root_cause: 
fix: 
verification: 
files_changed: []
