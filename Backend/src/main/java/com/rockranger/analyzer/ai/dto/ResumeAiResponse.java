package com.rockranger.analyzer.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResumeAiResponse {

    private StructuredResume structuredResume;
    private AnalysisDetails analysis;

    public ResumeAiResponse() {
        this.structuredResume = new StructuredResume();
        this.analysis = new AnalysisDetails();
    }

    public StructuredResume getStructuredResume() {
        return structuredResume;
    }

    public void setStructuredResume(StructuredResume structuredResume) {
        this.structuredResume = structuredResume;
    }

    public AnalysisDetails getAnalysis() {
        return analysis;
    }

    public void setAnalysis(AnalysisDetails analysis) {
        this.analysis = analysis;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StructuredResume {
        private Candidate candidate = new Candidate();
        private String summary = "";
        private Skills skills = new Skills();
        private List<Experience> experience = new ArrayList<>();
        private List<Project> projects = new ArrayList<>();
        private List<Education> education = new ArrayList<>();

        public Candidate getCandidate() {
            return candidate;
        }

        public void setCandidate(Candidate candidate) {
            this.candidate = candidate;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public Skills getSkills() {
            return skills;
        }

        public void setSkills(Skills skills) {
            this.skills = skills;
        }

        public List<Experience> getExperience() {
            return experience;
        }

        public void setExperience(List<Experience> experience) {
            this.experience = experience;
        }

        public List<Project> getProjects() {
            return projects;
        }

        public void setProjects(List<Project> projects) {
            this.projects = projects;
        }

        public List<Education> getEducation() {
            return education;
        }

        public void setEducation(List<Education> education) {
            this.education = education;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Candidate {
        private String name = "";
        private String email = "";
        private String phone = "";
        private String role = "";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Skills {
        private List<String> programmingLanguages = new ArrayList<>();
        private List<String> backend = new ArrayList<>();
        private List<String> frontend = new ArrayList<>();
        private List<String> database = new ArrayList<>();
        private List<String> tools = new ArrayList<>();

        public List<String> getProgrammingLanguages() {
            return programmingLanguages;
        }

        public void setProgrammingLanguages(List<String> programmingLanguages) {
            this.programmingLanguages = programmingLanguages;
        }

        public List<String> getBackend() {
            return backend;
        }

        public void setBackend(List<String> backend) {
            this.backend = backend;
        }

        public List<String> getFrontend() {
            return frontend;
        }

        public void setFrontend(List<String> frontend) {
            this.frontend = frontend;
        }

        public List<String> getDatabase() {
            return database;
        }

        public void setDatabase(List<String> database) {
            this.database = database;
        }

        public List<String> getTools() {
            return tools;
        }

        public void setTools(List<String> tools) {
            this.tools = tools;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Experience {
        private String company = "";
        private String role = "";
        private String duration = "";
        private List<String> responsibilities = new ArrayList<>();

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public List<String> getResponsibilities() {
            return responsibilities;
        }

        public void setResponsibilities(List<String> responsibilities) {
            this.responsibilities = responsibilities;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Project {
        private String name = "";
        private List<String> technologies = new ArrayList<>();
        private String description = "";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getTechnologies() {
            return technologies;
        }

        public void setTechnologies(List<String> technologies) {
            this.technologies = technologies;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Education {
        private String degree = "";
        private String college = "";
        private String cgpa = "";
        private String year = "";

        public String getDegree() {
            return degree;
        }

        public void setDegree(String degree) {
            this.degree = degree;
        }

        public String getCollege() {
            return college;
        }

        public void setCollege(String college) {
            this.college = college;
        }

        public String getCgpa() {
            return cgpa;
        }

        public void setCgpa(String cgpa) {
            this.cgpa = cgpa;
        }

        public String getYear() {
            return year;
        }

        public void setYear(String year) {
            this.year = year;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AnalysisDetails {
        private List<String> strengths = new ArrayList<>();
        private List<String> weaknesses = new ArrayList<>();
        private List<String> improvements = new ArrayList<>();
        private List<String> missingKeywords = new ArrayList<>();
        private Map<String, Integer> sectionScores = new HashMap<>();
        private Integer overallScore = 0;

        public List<String> getStrengths() {
            return strengths;
        }

        public void setStrengths(List<String> strengths) {
            this.strengths = strengths;
        }

        public List<String> getWeaknesses() {
            return weaknesses;
        }

        public void setWeaknesses(List<String> weaknesses) {
            this.weaknesses = weaknesses;
        }

        public List<String> getImprovements() {
            return improvements;
        }

        public void setImprovements(List<String> improvements) {
            this.improvements = improvements;
        }

        public List<String> getMissingKeywords() {
            return missingKeywords;
        }

        public void setMissingKeywords(List<String> missingKeywords) {
            this.missingKeywords = missingKeywords;
        }

        public Map<String, Integer> getSectionScores() {
            return sectionScores;
        }

        public void setSectionScores(Map<String, Integer> sectionScores) {
            this.sectionScores = sectionScores;
        }

        public Integer getOverallScore() {
            return overallScore;
        }

        public void setOverallScore(Integer overallScore) {
            this.overallScore = overallScore;
        }
    }
}
