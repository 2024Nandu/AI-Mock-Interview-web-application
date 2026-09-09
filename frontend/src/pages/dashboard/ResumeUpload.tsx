import { useState, useRef } from 'react';
import { motion } from 'framer-motion';
import { useNavigate } from 'react-router-dom';
import { Upload, File, X, CheckCircle, AlertCircle, Loader2, Brain, BarChart3 } from 'lucide-react';
import { resumeService } from '../../services/resumeService';

const ResumeUpload = () => {
  const navigate = useNavigate();
  const [file, setFile] = useState<File | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [uploadStatus, setUploadStatus] = useState<'idle' | 'uploading' | 'success' | 'error'>('idle');
  const [errorMessage, setErrorMessage] = useState('');
  const [, setUploadedResumeId] = useState<number | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const allowedTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'text/plain'];
  const maxSize = 5 * 1024 * 1024; // 5MB

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    const droppedFile = e.dataTransfer.files[0];
    validateAndSetFile(droppedFile);
  };

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = e.target.files?.[0];
    if (selectedFile) {
      validateAndSetFile(selectedFile);
    }
  };

  const validateAndSetFile = (file: File) => {
    setErrorMessage('');
    
    // Check file type
    if (!allowedTypes.includes(file.type)) {
      setErrorMessage('Please upload a PDF, DOC, DOCX, or TXT file.');
      return;
    }

    // Check file size
    if (file.size > maxSize) {
      setErrorMessage('File size must be less than 5MB.');
      return;
    }

    setFile(file);
    setUploadStatus('idle');
  };

  const handleRemoveFile = () => {
    setFile(null);
    setUploadStatus('idle');
    setErrorMessage('');
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleUpload = async () => {
    if (!file) return;

    setIsUploading(true);
    setUploadStatus('uploading');
    setUploadProgress(0);

    try {
      // Simulate progress
      const progressInterval = setInterval(() => {
        setUploadProgress((prev) => {
          if (prev >= 90) {
            clearInterval(progressInterval);
            return 90;
          }
          return prev + 10;
        });
      }, 300);

      const response = await resumeService.uploadResume(file);
      
      clearInterval(progressInterval);
      setUploadProgress(100);
      setUploadStatus('success');
      setUploadedResumeId(response.id);

      // Navigate to analysis after 2 seconds
      setTimeout(() => {
        navigate(`/dashboard/resume/${response.id}/analysis`);
      }, 2000);
    } catch (error: any) {
      console.error('Upload error:', error);
      setUploadStatus('error');
      setErrorMessage(error.response?.data?.message || 'Failed to upload resume. Please try again.');
    } finally {
      setIsUploading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Upload Resume</h1>
        <p className="text-gray-600 mt-2">
          Upload your resume to get ATS analysis and start mock interviews.
        </p>
      </div>

      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
        {/* Drag and Drop Area */}
        {!file && (
          <div
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            className={`border-2 border-dashed rounded-2xl p-12 text-center transition-all ${
              isDragging
                ? 'border-green-500 bg-green-50'
                : 'border-gray-300 hover:border-green-400 hover:bg-gray-50'
            }`}
          >
            <div className="flex flex-col items-center">
              <div className="w-20 h-20 bg-green-50 rounded-2xl flex items-center justify-center mb-4">
                <Upload className="w-10 h-10 text-green-600" />
              </div>
              <h3 className="text-lg font-semibold text-gray-900 mb-2">
                Drop your resume here
              </h3>
              <p className="text-gray-500 mb-4">
                or click to browse files
              </p>
              <button
                onClick={() => fileInputRef.current?.click()}
                className="px-6 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors"
              >
                Choose File
              </button>
              <input
                ref={fileInputRef}
                type="file"
                accept=".pdf,.doc,.docx,.txt"
                onChange={handleFileSelect}
                className="hidden"
              />
              <p className="text-xs text-gray-400 mt-4">
                Supports PDF, DOC, DOCX, TXT (Max 5MB)
              </p>
            </div>
          </div>
        )}

        {/* Selected File Preview */}
        {file && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="space-y-6"
          >
            <div className="flex items-center justify-between p-4 bg-gray-50 rounded-xl border border-gray-200">
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 bg-green-100 rounded-xl flex items-center justify-center">
                  <File className="w-6 h-6 text-green-600" />
                </div>
                <div>
                  <p className="font-medium text-gray-900">{file.name}</p>
                  <p className="text-sm text-gray-500">
                    {(file.size / 1024).toFixed(1)} KB • {file.type || 'Unknown type'}
                  </p>
                </div>
              </div>
              <button
                onClick={handleRemoveFile}
                disabled={isUploading}
                className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors disabled:opacity-50"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Error Message */}
            {errorMessage && (
              <div className="flex items-center gap-3 p-4 bg-red-50 rounded-xl border border-red-200">
                <AlertCircle className="w-5 h-5 text-red-600" />
                <p className="text-sm text-red-700">{errorMessage}</p>
              </div>
            )}

            {/* Upload Progress */}
            {uploadStatus === 'uploading' && (
              <div className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span className="text-gray-600">Uploading...</span>
                  <span className="text-gray-900 font-medium">{uploadProgress}%</span>
                </div>
                <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
                  <motion.div
                    initial={{ width: 0 }}
                    animate={{ width: `${uploadProgress}%` }}
                    className="h-full bg-green-600 rounded-full"
                  />
                </div>
              </div>
            )}

            {/* Success State */}
            {uploadStatus === 'success' && (
              <div className="flex items-center gap-3 p-4 bg-green-50 rounded-xl border border-green-200">
                <CheckCircle className="w-5 h-5 text-green-600" />
                <div>
                  <p className="text-sm font-medium text-green-700">Upload successful!</p>
                  <p className="text-sm text-green-600">Redirecting to analysis...</p>
                </div>
              </div>
            )}

            {/* Upload Button */}
            {uploadStatus === 'idle' && (
              <button
                onClick={handleUpload}
                disabled={isUploading}
                className="w-full py-3 bg-green-600 text-white rounded-xl font-medium hover:bg-green-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
              >
                {isUploading ? (
                  <>
                    <Loader2 className="w-5 h-5 animate-spin" />
                    Uploading...
                  </>
                ) : (
                  <>
                    <Upload className="w-5 h-5" />
                    Upload Resume
                  </>
                )}
              </button>
            )}

            {/* Retry Button */}
            {uploadStatus === 'error' && (
              <button
                onClick={() => {
                  setUploadStatus('idle');
                  setErrorMessage('');
                }}
                className="w-full py-3 bg-red-600 text-white rounded-xl font-medium hover:bg-red-700 transition-colors"
              >
                Try Again
              </button>
            )}
          </motion.div>
        )}
      </div>

      {/* Info Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-8">
        <div className="bg-white rounded-xl p-4 border border-gray-100">
          <div className="w-10 h-10 bg-blue-50 rounded-lg flex items-center justify-center mb-3">
            <File className="w-5 h-5 text-blue-600" />
          </div>
          <h4 className="font-medium text-gray-900">AI Analysis</h4>
          <p className="text-sm text-gray-500">Get detailed ATS score and resume feedback</p>
        </div>
        <div className="bg-white rounded-xl p-4 border border-gray-100">
          <div className="w-10 h-10 bg-purple-50 rounded-lg flex items-center justify-center mb-3">
            <Brain className="w-5 h-5 text-purple-600" />
          </div>
          <h4 className="font-medium text-gray-900">Mock Interviews</h4>
          <p className="text-sm text-gray-500">Practice with AI interviewer based on your resume</p>
        </div>
        <div className="bg-white rounded-xl p-4 border border-gray-100">
          <div className="w-10 h-10 bg-green-50 rounded-lg flex items-center justify-center mb-3">
            <BarChart3 className="w-5 h-5 text-green-600" />
          </div>
          <h4 className="font-medium text-gray-900">Track Progress</h4>
          <p className="text-sm text-gray-500">Monitor your improvement over time</p>
        </div>
      </div>
    </div>
  );
};

export default ResumeUpload;