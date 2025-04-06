# WebStorm Axios Generator Plugin

## Overview

The WebStorm Axios Generator Plugin is an innovative tool that automatically generates axios calls by learning from your repository using a Retrieval Augmented Generation (RAG) approach. It communicates with a backend server that returns both the file path for creating or updating code and the complete generated code. Additionally, the plugin provides a custom side-by-side diff view using a Longest Common Subsequence (LCS) algorithm—an essential feature since WebStorm lacks built-in support for diff visualization.

## Features

- **Automatic Axios Call Generation**: Utilizes a RAG approach to analyze your repository context and generate axios calls.
- **Server-Driven Updates**: Retrieves the target file path and the complete updated code from a server.
- **Custom Diff View**: Displays a side-by-side diff using an LCS algorithm, enabling clear visual comparisons of code changes.
- **Seamless WebStorm Integration**: Enhances your development workflow within the WebStorm IDE.
- **Enhanced Code Review**: Facilitates an easier review process by showing exactly what will change before applying updates.

## Installation

1. **Download the Plugin**:  
   Clone the project and run the plugin. It will open a separate Webstorm window

## Usage

1. **Trigger the Plugin**:  
   Launch the plugin from its designated menu or by using the assigned shortcut.

2. **Repository Analysis**:  
   The plugin analyzes your repository using the RAG approach to understand the context and code structure.

3. **Generate Axios Call**:  
   Upon activation, the plugin sends a request to the backend server. The server processes the repository context and returns:
   - The file path where the new or updated code should be saved.
   - The complete generated axios call code.

4. **Review Diff**:  
   The plugin presents a side-by-side diff view created with a custom LCS algorithm. This allows you to visually compare the generated code with the existing code before making any changes.

5. **Apply Changes**:  
   After reviewing the diff, apply the changes to update or create the file in your project.

## Configuration

- **Server Endpoint**:  
  Configure the server endpoint URL in the plugin settings to ensure proper communication.

- **Diff Settings**:  
  Adjust the settings for the diff view if necessary to better suit your review process.

- **Advanced RAG Options**:  
  Fine-tune any available parameters related to the RAG approach based on your repository and project needs.


